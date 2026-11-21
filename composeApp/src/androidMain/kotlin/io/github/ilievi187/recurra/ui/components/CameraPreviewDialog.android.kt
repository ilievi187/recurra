package io.github.ilievi187.recurra.ui.components

import androidx.compose.runtime.Composable
import io.github.ilievi187.recurra.data.ScanOutcome

@Composable
actual fun CameraPreviewDialog(
    onDismiss: () -> Unit,
    onCaptured: (ScanOutcome) -> Unit,
) {
    // Android uses native ActivityResultContracts.TakePicture via DocumentScanner.captureFromCamera
}
