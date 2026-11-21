package io.github.ilievi187.recurra.ui.components

import androidx.compose.runtime.Composable
import io.github.ilievi187.recurra.data.ScanOutcome

/**
 * Live webcam viewfinder dialog on Desktop (Linux & Windows).
 * On Android, camera capture delegates directly to the native system Camera app.
 */
@Composable
expect fun CameraPreviewDialog(
    onDismiss: () -> Unit,
    onCaptured: (ScanOutcome) -> Unit,
)
