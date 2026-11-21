package io.github.ilievi187.recurra.data

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.concurrent.thread

actual object DocumentScanner {

    private fun attachmentsDir(): File = File(VaultStorage.vaultDirectoryPath(), "attachments").apply {
        if (!exists()) mkdirs()
    }

    actual fun pickAndScanDocument(onResult: (ScanOutcome) -> Unit) {
        thread(start = true, isDaemon = true, name = "recurra-doc-scan") {
            val selected = pickFileNative()
            if (selected == null) {
                return@thread
            }

            val savedPath = runCatching {
                val safe = selected.name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val dest = File(attachmentsDir(), "${System.currentTimeMillis()}-$safe")
                Files.copy(selected.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
                dest.absolutePath
            }.getOrDefault(selected.absolutePath)

            val ext = selected.extension.lowercase()
            val outcome = when (ext) {
                "pdf" -> extractFromPdf(selected, savedPath)
                "txt" -> ScanOutcome(
                    rawText = runCatching { selected.readText(Charsets.UTF_8) }.getOrDefault(""),
                    attachmentPath = savedPath,
                    engineNote = "Read plain text file",
                )
                "jpg", "jpeg", "png", "webp", "bmp", "tif", "tiff" -> extractWithTesseract(selected, savedPath)
                else -> ScanOutcome(
                    rawText = "",
                    attachmentPath = savedPath,
                    engineNote = "Unsupported format ($ext) — attached file only",
                    error = "Unsupported format .$ext (use JPG, PNG, PDF, or TXT).",
                )
            }
            onResult(outcome)
        }
    }

    private fun pickFileNative(): File? {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("linux")) {
            // Wayland/Hyprland/X11-safe native portal file pickers (avoids AWT FileDialog off-EDT XWayland crashes)
            if (isCommandAvailable("zenity")) {
                return pickWithExternalCommand(
                    listOf(
                        "zenity",
                        "--file-selection",
                        "--title=Select Receipt, Invoice, or Warranty",
                        "--file-filter=Receipts & Invoices | *.jpg *.jpeg *.png *.webp *.pdf *.txt *.JPG *.PNG *.PDF",
                        "--file-filter=All files | *",
                    ),
                )
            }

            if (isCommandAvailable("kdialog")) {
                return pickWithExternalCommand(
                    listOf(
                        "kdialog",
                        "--getopenfilename",
                        System.getProperty("user.home"),
                        "*.jpg *.jpeg *.png *.webp *.pdf *.txt|Receipts & Invoices",
                    ),
                )
            }

            // Safe Swing JFileChooser fallback strictly on the AWT EDT
            return pickWithSwingOnEdt()
        }

        // Windows & macOS: native FileDialog strictly on the AWT EDT
        var chosen: File? = null
        runCatching {
            val task = Runnable {
                val dialog = FileDialog(null as Frame?, "Select Receipt, Invoice, or Warranty", FileDialog.LOAD)
                dialog.setFilenameFilter { _, name ->
                    val l = name.lowercase()
                    l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") ||
                        l.endsWith(".webp") || l.endsWith(".pdf") || l.endsWith(".txt")
                }
                dialog.isVisible = true
                val dir = dialog.directory
                val file = dialog.file
                dialog.dispose()
                if (dir != null && file != null) {
                    chosen = File(dir, file)
                }
            }
            if (SwingUtilities.isEventDispatchThread()) {
                task.run()
            } else {
                SwingUtilities.invokeAndWait(task)
            }
        }
        return chosen
    }

    private fun isCommandAvailable(binary: String): Boolean {
        return runCatching {
            val proc = ProcessBuilder("which", binary).redirectErrorStream(true).start()
            proc.waitFor() == 0
        }.getOrDefault(false)
    }

    private fun pickWithExternalCommand(cmd: List<String>): File? {
        return runCatching {
            val proc = ProcessBuilder(cmd).redirectErrorStream(false).start()
            val out = proc.inputStream.bufferedReader(Charsets.UTF_8).readText().trim()
            val code = proc.waitFor()
            if (code == 0 && out.isNotBlank()) {
                val f = File(out)
                if (f.exists() && f.isFile) f else null
            } else {
                null
            }
        }.getOrNull()
    }

    private fun pickWithSwingOnEdt(): File? {
        var chosen: File? = null
        runCatching {
            val task = Runnable {
                val chooser = JFileChooser(System.getProperty("user.home")).apply {
                    dialogTitle = "Select Receipt, Invoice, or Warranty"
                    fileFilter = FileNameExtensionFilter(
                        "Receipts & Invoices (JPG, PNG, WEBP, PDF, TXT)",
                        "jpg", "jpeg", "png", "webp", "pdf", "txt",
                    )
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    chosen = chooser.selectedFile
                }
            }
            if (SwingUtilities.isEventDispatchThread()) {
                task.run()
            } else {
                SwingUtilities.invokeAndWait(task)
            }
        }
        return chosen
    }

    private fun extractFromPdf(file: File, savedPath: String): ScanOutcome {
        return runCatching {
            Loader.loadPDF(file).use { doc ->
                val text = PDFTextStripper().getText(doc).orEmpty().trim()
                if (text.isNotBlank()) {
                    ScanOutcome(
                        rawText = text,
                        attachmentPath = savedPath,
                        engineNote = "Extracted text from PDF (Apache PDFBox, 100% offline)",
                    )
                } else {
                    ScanOutcome(
                        rawText = "",
                        attachmentPath = savedPath,
                        engineNote = "Scanned/image-only PDF attached",
                        error = "This PDF appears to be a scanned image without embedded text. Export as PNG/JPG for OCR or fill details manually.",
                    )
                }
            }
        }.getOrElse { err ->
            ScanOutcome(
                rawText = "",
                attachmentPath = savedPath,
                engineNote = "PDF attached",
                error = "Could not read PDF text: ${err.message}",
            )
        }
    }

    private fun extractWithTesseract(file: File, savedPath: String): ScanOutcome {
        val candidates = listOf(
            listOf("tesseract", file.absolutePath, "stdout", "-l", "bul+eng", "--psm", "6"),
            listOf("tesseract", file.absolutePath, "stdout", "-l", "eng", "--psm", "6"),
            listOf("tesseract", file.absolutePath, "stdout"),
        )
        for (cmd in candidates) {
            val result = runCatching {
                val proc = ProcessBuilder(cmd).redirectErrorStream(false).start()
                val out = proc.inputStream.bufferedReader(Charsets.UTF_8).readText()
                val code = proc.waitFor()
                if (code == 0 && out.isNotBlank()) out.trim() else null
            }.getOrNull()
            if (!result.isNullOrBlank()) {
                val langNote = if (cmd.contains("bul+eng")) "Bulgarian + English" else "English"
                return ScanOutcome(
                    rawText = result,
                    attachmentPath = savedPath,
                    engineNote = "Scanned offline via Tesseract OCR ($langNote)",
                )
            }
        }
        return ScanOutcome(
            rawText = "",
            attachmentPath = savedPath,
            engineNote = "Image attached (Tesseract OCR not found on system)",
            error = "Photo attached! To auto-read text from images, install Tesseract OCR (Arch: sudo pacman -S tesseract tesseract-data-bul tesseract-data-eng | Ubuntu: sudo apt install tesseract-ocr tesseract-ocr-bul | Windows: winget install UB-Mannheim.TesseractOCR).",
        )
    }

    actual fun openAttachment(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false
        return runCatching {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file)
                true
            } else {
                val os = System.getProperty("os.name").lowercase()
                when {
                    os.contains("linux") -> {
                        ProcessBuilder("xdg-open", file.absolutePath).start()
                        true
                    }
                    os.contains("win") -> {
                        ProcessBuilder("explorer", file.absolutePath).start()
                        true
                    }
                    else -> false
                }
            }
        }.getOrDefault(false)
    }
}
