package io.github.ilievi187.recurra.data

data class ScanOutcome(
    val rawText: String,
    val attachmentPath: String,
    val engineNote: String,
    val error: String? = null,
)

expect object DocumentScanner {
    fun pickAndScanDocument(onResult: (ScanOutcome) -> Unit)
    fun openAttachment(filePath: String): Boolean
}
