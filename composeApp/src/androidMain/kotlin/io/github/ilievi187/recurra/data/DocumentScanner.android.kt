package io.github.ilievi187.recurra.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

actual object DocumentScanner {

    private var appContext: Context? = null
    private var launchPicker: (() -> Unit)? = null
    private var launchCamera: ((Uri) -> Unit)? = null
    private var pendingCallback: ((ScanOutcome) -> Unit)? = null
    private var pendingCameraFile: File? = null
    private var pendingCameraUri: Uri? = null

    fun bind(
        context: Context,
        pickerLauncher: () -> Unit,
        cameraLauncher: (Uri) -> Unit,
    ) {
        appContext = context.applicationContext
        launchPicker = pickerLauncher
        launchCamera = cameraLauncher
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
            runMlKitOcr(ctx, uri, destFile.absolutePath, "Uploaded image · ML Kit OCR", cb)
            return
        }

        cb(
            ScanOutcome(
                rawText = "",
                attachmentPath = destFile.absolutePath,
                engineNote = "File attached locally",
                error = "File attached! For automatic OCR on Android, upload a photo (JPG/PNG) or use Take Photo.",
            ),
        )
    }

    fun onCameraCaptured(success: Boolean) {
        val cb = pendingCallback
        val file = pendingCameraFile
        val uri = pendingCameraUri
        pendingCallback = null
        pendingCameraFile = null
        pendingCameraUri = null
        val ctx = appContext

        if (!success || cb == null || file == null || uri == null || ctx == null || !file.exists()) {
            file?.delete()
            return
        }
        runMlKitOcr(ctx, uri, file.absolutePath, "Camera photo · ML Kit OCR", cb)
    }

    private fun runMlKitOcr(
        ctx: Context,
        uri: Uri,
        attachmentPath: String,
        engineNote: String,
        cb: (ScanOutcome) -> Unit,
    ) {
        runCatching {
            val image = InputImage.fromFilePath(ctx, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    cb(
                        ScanOutcome(
                            rawText = visionText.text.trim(),
                            attachmentPath = attachmentPath,
                            engineNote = engineNote,
                        ),
                    )
                }
                .addOnFailureListener { err ->
                    cb(
                        ScanOutcome(
                            rawText = "",
                            attachmentPath = attachmentPath,
                            engineNote = "Photo attached",
                            error = "OCR failed: ${err.message}",
                        ),
                    )
                }
        }.onFailure { err ->
            cb(ScanOutcome("", attachmentPath, "Photo attached", "Could not read image: ${err.message}"))
        }
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

    actual fun captureFromCamera(onResult: (ScanOutcome) -> Unit) {
        val ctx = appContext
        val launcher = launchCamera
        if (ctx == null || launcher == null) {
            onResult(ScanOutcome("", "", "Camera unavailable", "Camera launcher is not ready."))
            return
        }
        runCatching {
            val attachmentsDir = File(VaultStorage.vaultDirectoryPath(), "attachments").apply {
                if (!exists()) mkdirs()
            }
            val photoFile = File(attachmentsDir, "${System.currentTimeMillis()}-camera.jpg")
            val authority = "${ctx.packageName}.fileprovider"
            val photoUri = FileProvider.getUriForFile(ctx, authority, photoFile)
            pendingCallback = onResult
            pendingCameraFile = photoFile
            pendingCameraUri = photoUri
            launcher(photoUri)
        }.onFailure { err ->
            pendingCallback = null
            onResult(ScanOutcome("", "", "Camera error", "Could not open camera: ${err.message}"))
        }
    }

    actual fun openAttachment(filePath: String): Boolean {
        return File(filePath).exists()
    }
}
