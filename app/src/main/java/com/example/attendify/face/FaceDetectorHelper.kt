package com.example.attendify.face


import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class FaceDetectionResult(
    val faceCount: Int,
    val boundingBox: Rect?
)

class FaceDetectorHelper {
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()

    private val detector = FaceDetection.getClient(options)

    @androidx.camera.core.ExperimentalGetImage
    suspend fun analyze(imageProxy: ImageProxy): FaceDetectionResult {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return FaceDetectionResult(0, null)
        }
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        return suspendCancellableCoroutine { continuation ->
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    val result = FaceDetectionResult(
                        faceCount = faces.size,
                        boundingBox = faces.firstOrNull()?.boundingBox
                    )
                    if (continuation.isActive) continuation.resume(result)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(FaceDetectionResult(0, null))
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}