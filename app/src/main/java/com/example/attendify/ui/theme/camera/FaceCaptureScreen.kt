package com.example.attendify.ui.theme.camera


import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.attendify.face.FaceDetectionResult
import com.example.attendify.face.FaceDetectorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

data class CapturedFrame(val bitmap: Bitmap, val faceBoundingBox: Rect)

@androidx.camera.core.ExperimentalGetImage
@Composable
fun FaceCaptureScreen(
    instructionText: String,
    onCapture: (CapturedFrame) -> Unit,
    onCancel: () -> Unit
) {
    RequiresCameraPermission {
        FaceCaptureContent(instructionText, onCapture, onCancel)
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
private fun FaceCaptureContent(
    instructionText: String,
    onCapture: (CapturedFrame) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var detectionResult by remember { mutableStateOf(FaceDetectionResult(0, null)) }
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val faceDetector = remember { FaceDetectorHelper() }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val statusText = when (detectionResult.faceCount) {
        0 -> "No face detected"
        1 -> "Face detected — ready"
        else -> "Multiple faces detected — only one person allowed"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        val capture = ImageCapture.Builder().build()
                        imageCapture = capture

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                            scope.launch(Dispatchers.Default) {
                                val result = faceDetector.analyze(imageProxy)
                                detectionResult = result
                            }
                        }

                        val selector = CameraSelector.DEFAULT_FRONT_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, selector, preview, capture, analysis
                            )
                        } catch (e: Exception) {
                            // Binding failed (e.g. no front camera) — status text will
                            // stay "No face detected" and capture stays disabled, so
                            // the screen degrades safely instead of crashing.
                        }
                    }, ContextCompat_getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(instructionText, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                statusText,
                color = if (detectionResult.faceCount == 1)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    enabled = detectionResult.faceCount == 1,
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        val box = detectionResult.boundingBox ?: return@Button
                        capture.takePicture(
                            analysisExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = image.toBitmap()
                                    image.close()

                                    scope.launch(Dispatchers.Main) {
                                        onCapture(CapturedFrame(bitmap, box))
                                    }
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    // Capture failed — user stays on screen and can retry.
                                }
                            }
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Capture")
                }
            }
        }
    }
}

private fun ContextCompat_getMainExecutor(ctx: android.content.Context) =
    androidx.core.content.ContextCompat.getMainExecutor(ctx)