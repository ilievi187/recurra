package io.github.ilievi187.recurra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.data.DocumentScanner
import io.github.ilievi187.recurra.data.ScanOutcome
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import kotlin.concurrent.thread

@Composable
actual fun CameraPreviewDialog(
    onDismiss: () -> Unit,
    onCaptured: (ScanOutcome) -> Unit,
) {
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var latestJpeg by remember { mutableStateOf<ByteArray?>(null) }
    var statusText by remember { mutableStateOf("Starting camera...") }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val active = AtomicBoolean(true)
        var ffmpegProc: Process? = null

        val worker = thread(start = true, isDaemon = true, name = "recurra-live-camera") {
            val cmd = buildLiveCameraCommand()
            if (cmd == null) {
                cameraError = "No webcam or ffmpeg detected on this system."
                return@thread
            }

            try {
                val proc = ProcessBuilder(cmd).redirectErrorStream(false).start()
                ffmpegProc = proc
                val input = BufferedInputStream(proc.inputStream, 65536)
                val frameBuf = ByteArrayOutputStream(65536)
                var prev = -1
                var inFrame = false

                while (active.get()) {
                    val b = input.read()
                    if (b == -1) break
                    if (!inFrame) {
                        if (prev == 0xFF && b == 0xD8) {
                            frameBuf.reset()
                            frameBuf.write(0xFF)
                            frameBuf.write(0xD8)
                            inFrame = true
                        }
                    } else {
                        frameBuf.write(b)
                        if (prev == 0xFF && b == 0xD9) {
                            val bytes = frameBuf.toByteArray()
                            inFrame = false
                            if (bytes.size > 1000 && active.get()) {
                                val bmp = runCatching {
                                    ImageIO.read(ByteArrayInputStream(bytes))?.toComposeImageBitmap()
                                }.getOrNull()
                                if (bmp != null && active.get()) {
                                    latestJpeg = bytes
                                    previewBitmap = bmp
                                    statusText = "Hold your receipt or warranty steady, then click Capture & Scan."
                                }
                            }
                        }
                    }
                    prev = b
                }
                if (active.get() && previewBitmap == null) {
                    cameraError = "Could not read video frames from webcam. Make sure no other app is using it."
                }
            } catch (err: Exception) {
                if (active.get()) {
                    cameraError = "Camera error: ${err.message}"
                }
            } finally {
                runCatching { ffmpegProc?.destroyForcibly() }
            }
        }

        onDispose {
            active.set(false)
            runCatching { ffmpegProc?.destroyForcibly() }
            runCatching { worker.interrupt() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Rounded.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Camera Scanner")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141815))
                        .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    val bmp = previewBitmap
                    when {
                        cameraError != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(20.dp),
                            ) {
                                Icon(Icons.Rounded.VideocamOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
                                Text(cameraError!!, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        bmp != null -> {
                            Image(
                                bitmap = bmp,
                                contentDescription = "Live webcam preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            // Subtle framing guide box
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.84f)
                                    .border(
                                        BorderStroke(1.5.dp, Color.White.copy(alpha = 0.55f)),
                                        RoundedCornerShape(12.dp),
                                    ),
                            )
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(statusText, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (cameraError == null) {
                    Text(
                        text = if (isScanning) "Reading receipt text offline..." else statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val jpeg = latestJpeg ?: return@Button
                    isScanning = true
                    thread(start = true, isDaemon = true, name = "recurra-camera-ocr") {
                        val outcome = DocumentScanner.saveAndScanCameraJpeg(jpeg)
                        onCaptured(outcome)
                    }
                },
                enabled = latestJpeg != null && !isScanning,
            ) {
                Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isScanning) "Scanning..." else "Capture & Scan", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun buildLiveCameraCommand(): List<String>? {
    val os = System.getProperty("os.name").lowercase()
    if (os.contains("linux")) {
        val dev = (0..3).map { "/dev/video$it" }.firstOrNull { File(it).exists() } ?: return null
        return listOf(
            "ffmpeg", "-loglevel", "quiet",
            "-f", "v4l2", "-i", dev,
            "-f", "image2pipe", "-vcodec", "mjpeg", "-q:v", "4", "-r", "15", "-",
        )
    }
    if (os.contains("win")) {
        val camName = runCatching {
            val proc = ProcessBuilder("ffmpeg", "-list_devices", "true", "-f", "dshow", "-i", "dummy")
                .redirectErrorStream(true)
                .start()
            val out = proc.inputStream.bufferedReader(Charsets.UTF_8).readText()
            proc.waitFor()
            Regex("\"([^\"]+)\"\\s*\\(video\\)").find(out)?.groupValues?.get(1)
        }.getOrNull() ?: return null
        return listOf(
            "ffmpeg", "-loglevel", "quiet",
            "-f", "dshow", "-i", "video=$camName",
            "-f", "image2pipe", "-vcodec", "mjpeg", "-q:v", "4", "-r", "15", "-",
        )
    }
    return null
}
