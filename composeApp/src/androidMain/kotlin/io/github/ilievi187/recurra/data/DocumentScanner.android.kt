package io.github.ilievi187.recurra.data

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

actual object DocumentScanner {

    private var appContext: Context? = null
    private var launchPicker: (() -> Unit)? = null
    private var pendingCallback: ((ScanOutcome) -> Unit)? = null

    fun bind(context: Context, pickerLauncher: () -> Unit) {
        appContext = context.applicationContext
        launchPicker = pickerLauncher
    }

    fun onDocumentPicked(uri: Uri?) {
        val cb = pendingCallback
        pendingCallback = null
        val ctx = appContext
        if (uri == null || cb == null || ctx == null) return

        val attachmentsDir = File(VaultStorage.vaultDirectoryPath(), "attachments").apply {
            if (!exists()) mkdirs()
        }
        val mime = ctx.contentResolver.getType(uri).orEmpty().lowercase()
        val ext = when {
            mime.contains("pdf") -> "pdf"
            mime.contains("png") -> "png"
            mime.contains("text") -> "txt"
            else -> "jpg"
        }
        val destFile = File(attachmentsDir, "${System.currentTimeMillis()}-receipt.$ext")
        runCatching {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
        }

        if (mime.contains("text")) {
            val text = runCatching { destFile.readText(Charsets.UTF_8) }.getOrDefault("")
            cb(ScanOutcome(text, destFile.absolutePath, "Read text file"))
            return
        }

        if (mime.contains("image") || ext == "jpg" || ext == "png") {
            runCatching {
                val image = InputImage.fromFilePath(ctx, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        cb(
                            ScanOutcome(
                                rawText = visionText.text.trim(),
                                attachmentPath = destFile.absolutePath,
                                engineNote = "Scanned offline via on-device ML Kit OCR",
                            ),
                        )
                    }
                    .addOnFailureListener { err ->
                        cb(
                            ScanOutcome(
                                rawText = "",
                                attachmentPath = destFile.absolutePath,
                                engineNote = "Image attached",
                                error = "OCR failed: ${err.message}",
                            ),
                        )
                    }
            }.onFailure { err ->
                cb(ScanOutcome("", destFile.absolutePath, "Image attached", "Could not open image: ${err.message}"))
            }
            return
        }

        cb(
            ScanOutcome(
                rawText = "",
                attachmentPath = destFile.absolutePath,
                engineNote = "File attached locally",
                error = "File attached! For automatic OCR on Android, select a receipt/warranty photo (JPG/PNG).",
            ),
        )
    }

    actual fun pickAndScanDocument(onResult: (ScanOutcome) -> Unit) {
        pendingCallback = onResult
        val launcher = launchPicker
        if (launcher != null) {
            launcher()
        } else {
            pendingCallback = null
            onResult(ScanOutcome("", "", "Picker unavailable", "Document picker is not ready."))
        }
    }

    actual fun openAttachment(filePath: String): Boolean {
        return File(filePath).exists()
    }
}
