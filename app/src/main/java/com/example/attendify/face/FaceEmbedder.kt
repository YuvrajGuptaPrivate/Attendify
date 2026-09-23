package com.example.attendify.face


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sqrt
import java.io.FileInputStream
import java.nio.channels.FileChannel
/**
 * Generates a 128-dim FaceNet embedding for a detected face using a bundled
 * TFLite model (facenet.tflite, from shubham0204/FaceRecognition_With_FaceNet_Android,
 * Apache-2.0). Face is aligned using eye landmarks, cropped, resized to
 * 160x160, and "prewhitened" (per-image mean/std normalization) before
 * being fed to the model, matching FaceNet's expected preprocessing.
 */
class FaceEmbedder(context: Context) {

    companion object {
        private const val MODEL_FILE = "facenet.tflite"
        private const val INPUT_SIZE = 160
        private const val EMBEDDING_SIZE = 128
    }

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .build()
    private val detector = FaceDetection.getClient(options)

    private val interpreter: Interpreter by lazy {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)

        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)

        val model = inputStream.channel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )

        inputStream.close()

        Interpreter(
            model,
            Interpreter.Options().apply {
                setNumThreads(4)
            }
        )
    }

    suspend fun embed(bitmap: Bitmap): FloatArray? {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val faces: List<Face> = suspendCancellableCoroutine { continuation ->
            detector.process(inputImage)
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                .addOnFailureListener { if (continuation.isActive) continuation.resume(emptyList()) }
        }
        val face = faces.firstOrNull() ?: return null
        val box = face.boundingBox
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position

        val aligned = if (leftEye != null && rightEye != null) {
            val angleDegrees = Math.toDegrees(
                atan2((rightEye.y - leftEye.y).toDouble(), (rightEye.x - leftEye.x).toDouble())
            ).toFloat()
            val pivotX = (leftEye.x + rightEye.x) / 2f
            val pivotY = (leftEye.y + rightEye.y) / 2f
            val matrix = Matrix().apply { postRotate(-angleDegrees, pivotX, pivotY) }
            try {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } catch (e: Exception) {
                bitmap
            }
        } else bitmap

        val marginX = (box.width() * 0.2f).toInt()
        val marginY = (box.height() * 0.2f).toInt()
        val left = (box.left - marginX).coerceIn(0, aligned.width - 1)
        val top = (box.top - marginY).coerceIn(0, aligned.height - 1)
        val right = (box.right + marginX).coerceIn(left + 1, aligned.width)
        val bottom = (box.bottom + marginY).coerceIn(top + 1, aligned.height)
        val cropped = try {
            Bitmap.createBitmap(aligned, left, top, right - left, bottom - top)
        } catch (e: Exception) {
            return null
        }

        val resized = Bitmap.createScaledBitmap(cropped, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = bitmapToPrewhitenedBuffer(resized)

        val output = Array(1) { FloatArray(EMBEDDING_SIZE) }
        interpreter.run(inputBuffer, output)
        return output[0]
    }

    /** FaceNet's standard preprocessing: per-image standardization ("prewhiten"). */
    private fun bitmapToPrewhitenedBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        val rgb = FloatArray(pixels.size * 3)
        var sum = 0f
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            rgb[i * 3] = r.toFloat()
            rgb[i * 3 + 1] = g.toFloat()
            rgb[i * 3 + 2] = b.toFloat()
            sum += r + g + b
        }
        val mean = sum / rgb.size
        var variance = 0f
        for (v in rgb) variance += (v - mean) * (v - mean)
        val std = max(sqrt(variance / rgb.size), 1f / sqrt(rgb.size.toFloat()))

        for (v in rgb) buffer.putFloat((v - mean) / std)
        buffer.rewind()
        return buffer
    }
}