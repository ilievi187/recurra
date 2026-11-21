package io.github.ilievi187.recurra.data

data class ScanOutcome(
    val rawText: String,
    val attachmentPath: String,
    val engineNote: String,
    val error: String? = null,
)

expect object DocumentScanner {
    /** Opens the file/gallery picker to upload an existing photo or PDF. */
    fun pickAndScanDocument(onResult: (ScanOutcome) -> Unit)

    /** Launches the available camera to capture a photo of a receipt/invoice/warranty. */
    fun captureFromCamera(onResult: (ScanOutcome) -> Unit)

    fun openAttachment(filePath: String): Boolean
}
