#!/usr/bin/env bash
set -euo pipefail

# Packages the Compose Desktop Linux distributable into:
#   1. A universal AppImage (with Wayland/Hyprland/tiling-WM fixes + AppStream metadata)
#   2. A clean portable tar.gz archive (for Arch PKGBUILD, NixOS, Void, Gentoo, manual installs)
# Usage: ./packaging/linux/build-appimage.sh <version>

VERSION="${1:-1.1.0}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DIST_DIR="${ROOT_DIR}/composeApp/build/compose/binaries/main/app/Recurra"
if [ ! -d "${DIST_DIR}" ]; then
  DIST_DIR="${ROOT_DIR}/composeApp/build/compose/binaries/main/app/recurra"
fi
WORK_DIR="${ROOT_DIR}/build/appimage"
APPDIR="${WORK_DIR}/Recurra.AppDir"
TAR_STAGE="${WORK_DIR}/tarstage/Recurra"
OUT_APPIMAGE="${ROOT_DIR}/build/Recurra-${VERSION}-x86_64.AppImage"
OUT_TARBALL="${ROOT_DIR}/build/Recurra-${VERSION}-linux-x86_64.tar.gz"

if [ ! -d "${DIST_DIR}" ]; then
  echo "ERROR: Compose distributable not found under composeApp/build/compose/binaries/main/app/"
  echo "Run: gradle :composeApp:createDistributable first."
  exit 1
fi

rm -rf "${APPDIR}" "${WORK_DIR}/tarstage"
mkdir -p "${APPDIR}/usr/share/metainfo" "${APPDIR}/usr/share/icons/hicolor/256x256/apps" "${TAR_STAGE}"

cp -a "${DIST_DIR}/." "${APPDIR}/usr/"
cp -a "${DIST_DIR}/." "${TAR_STAGE}/"

BIN_NAME="Recurra"
if [ ! -x "${APPDIR}/usr/bin/${BIN_NAME}" ]; then
  BIN_NAME="recurra"
fi

# Desktop entry, icon, and AppStream metadata
cp "${ROOT_DIR}/packaging/linux/recurra.desktop" "${APPDIR}/recurra.desktop"
sed -i "s|^Exec=.*|Exec=${BIN_NAME}|" "${APPDIR}/recurra.desktop"
cp "${ROOT_DIR}/packaging/icons/recurra.png" "${APPDIR}/recurra.png"
cp "${ROOT_DIR}/packaging/icons/recurra.png" "${APPDIR}/usr/share/icons/hicolor/256x256/apps/recurra.png"
cp "${ROOT_DIR}/packaging/linux/io.github.ilievi187.recurra.metainfo.xml" \
   "${APPDIR}/usr/share/metainfo/io.github.ilievi187.recurra.metainfo.xml"
ln -sf "recurra.png" "${APPDIR}/.DirIcon"

# Smart universal Linux AppRun wrapper:
# - Fixes blank windows on Hyprland/Sway/i3/bspwm/dwm via _JAVA_AWT_WM_NONREPARENTING=1
# - Enables crisp system font anti-aliasing
# - Auto-recovers from non-standard XWayland socket names (e.g. /tmp/.X11-unix/X0_)
cat > "${APPDIR}/AppRun" << EOF
#!/usr/bin/env bash
HERE="\$(dirname "\$(readlink -f "\${0}")")"
export _JAVA_AWT_WM_NONREPARENTING=1
export _JAVA_OPTIONS="\${_JAVA_OPTIONS:-} -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true"

if [ -n "\${DISPLAY:-}" ] && [[ "\${DISPLAY}" =~ ^:([0-9]+)\$ ]]; then
  dnum="\${BASH_REMATCH[1]}"
  if [ ! -S "/tmp/.X11-unix/X\${dnum}" ]; then
    for sock in "/tmp/.X11-unix/X\${dnum}_" /tmp/.X11-unix/X*; do
      if [ -S "\${sock}" ]; then
        export DISPLAY="\${sock}"
        break
      fi
    done
  fi
fi

exec "\${HERE}/usr/bin/${BIN_NAME}" "\$@"
EOF
chmod +x "${APPDIR}/AppRun"

# Also stage portable tar.gz with desktop entry, icon, metainfo, license, and launcher script
cp "${ROOT_DIR}/packaging/linux/recurra.desktop" "${TAR_STAGE}/recurra.desktop"
cp "${ROOT_DIR}/packaging/icons/recurra.png" "${TAR_STAGE}/recurra.png"
cp "${ROOT_DIR}/packaging/linux/io.github.ilievi187.recurra.metainfo.xml" "${TAR_STAGE}/io.github.ilievi187.recurra.metainfo.xml"
cp "${ROOT_DIR}/LICENSE" "${TAR_STAGE}/LICENSE"
cp "${APPDIR}/AppRun" "${TAR_STAGE}/launch-recurra.sh"
sed -i 's|${HERE}/usr/bin/|${HERE}/bin/|g' "${TAR_STAGE}/launch-recurra.sh"
chmod +x "${TAR_STAGE}/launch-recurra.sh"

tar -C "${WORK_DIR}/tarstage" -czf "${OUT_TARBALL}" "Recurra"
echo "Built portable tarball: ${OUT_TARBALL}"

APPIMAGETOOL="${WORK_DIR}/appimagetool-x86_64.AppImage"
if [ ! -x "${APPIMAGETOOL}" ]; then
  echo "Downloading appimagetool..."
  curl -fsSL -o "${APPIMAGETOOL}" \
    "https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage"
  chmod +x "${APPIMAGETOOL}"
fi

ARCH=x86_64 APPIMAGE_EXTRACT_AND_RUN=1 "${APPIMAGETOOL}" "${APPDIR}" "${OUT_APPIMAGE}"
chmod +x "${OUT_APPIMAGE}"
echo "Built AppImage: ${OUT_APPIMAGE}"
