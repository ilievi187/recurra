#!/usr/bin/env bash
set -euo pipefail

# Packages the Compose Desktop Linux distributable into a portable AppImage.
# Usage: ./packaging/linux/build-appimage.sh <version>

VERSION="${1:-1.0.0}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DIST_DIR="${ROOT_DIR}/composeApp/build/compose/binaries/main/app/recurra"
WORK_DIR="${ROOT_DIR}/build/appimage"
APPDIR="${WORK_DIR}/Recurra.AppDir"
OUT_FILE="${ROOT_DIR}/build/Recurra-${VERSION}-x86_64.AppImage"

if [ ! -d "${DIST_DIR}" ]; then
  echo "ERROR: Compose distributable not found at ${DIST_DIR}"
  echo "Run: ./gradlew :composeApp:createDistributable first."
  exit 1
fi

rm -rf "${APPDIR}"
mkdir -p "${APPDIR}/usr" "${WORK_DIR}"
cp -a "${DIST_DIR}/." "${APPDIR}/usr/"

cp "${ROOT_DIR}/packaging/linux/recurra.desktop" "${APPDIR}/recurra.desktop"
cp "${ROOT_DIR}/packaging/icons/recurra.png" "${APPDIR}/recurra.png"
ln -sf "recurra.png" "${APPDIR}/.DirIcon"

cat > "${APPDIR}/AppRun" << 'EOF'
#!/usr/bin/env bash
HERE="$(dirname "$(readlink -f "${0}")")"
exec "${HERE}/usr/bin/recurra" "$@"
EOF
chmod +x "${APPDIR}/AppRun"

APPIMAGETOOL="${WORK_DIR}/appimagetool-x86_64.AppImage"
if [ ! -x "${APPIMAGETOOL}" ]; then
  echo "Downloading appimagetool..."
  curl -fsSL -o "${APPIMAGETOOL}" \
    "https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage"
  chmod +x "${APPIMAGETOOL}"
fi

ARCH=x86_64 APPIMAGE_EXTRACT_AND_RUN=1 "${APPIMAGETOOL}" "${APPDIR}" "${OUT_FILE}"
chmod +x "${OUT_FILE}"
echo "Built AppImage: ${OUT_FILE}"
