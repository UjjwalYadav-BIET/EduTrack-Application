package com.example.edutrackapp.cms.feature.teacher_Module.attendance.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream

class FaceAnalyser(
    private val context: Context,
    private val onFaceDetected: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.05f)   // ✅ detect smaller/distant faces
            .enableTracking()
            .build()
    )

    private var isProcessing = false

    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }
        isProcessing = true

        val bitmap   = imageProxy.toBitmap()
        val rotation = imageProxy.imageInfo.rotationDegrees
        val rotated  = rotateBitmap(bitmap, rotation)

        val inputImage = InputImage.fromBitmap(rotated, 0)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val bestFace = faces
                    .filter { isFaceQualityGood(it, rotated) }
                    .maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }

                bestFace?.let { face ->
                    android.util.Log.d("FaceAnalyser", "✅ Face accepted, sending to model")
                    val cropped = cropFace(rotated, face)
                    if (cropped != null) onFaceDetected(cropped)
                }
            }
            .addOnFailureListener {
                android.util.Log.e("FaceAnalyser", "Detection failed: ${it.message}")
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }

    private fun isFaceQualityGood(face: Face, source: Bitmap): Boolean {
        val box       = face.boundingBox
        val faceArea  = box.width() * box.height()
        val frameArea = source.width * source.height

        // ✅ Lowered to 0.1% — works with wide-angle back camera
        val faceRatio = faceArea.toFloat() / frameArea.toFloat()
        if (faceRatio < 0.001f) {
            android.util.Log.d("FaceAnalyser", "Face too small: ratio=$faceRatio")
            return false
        }

        android.util.Log.d("FaceAnalyser", "Face ratio OK: $faceRatio")

        // Reject extreme head rotations
        val eulerY = face.headEulerAngleY
        val eulerZ = face.headEulerAngleZ
        if (Math.abs(eulerY) > 45f || Math.abs(eulerZ) > 45f) {
            android.util.Log.d("FaceAnalyser", "Bad angle: Y=$eulerY Z=$eulerZ")
            return false
        }

        return true
    }

    private fun cropFace(source: Bitmap, face: Face): Bitmap? {
        val box  = face.boundingBox
        val padX = (box.width()  * 0.25f).toInt()
        val padY = (box.height() * 0.25f).toInt()

        val left   = (box.left   - padX).coerceAtLeast(0)
        val top    = (box.top    - padY).coerceAtLeast(0)
        val right  = (box.right  + padX).coerceAtMost(source.width)
        val bottom = (box.bottom + padY).coerceAtMost(source.height)

        val w = right  - left
        val h = bottom - top
        if (w <= 0 || h <= 0) return null

        return Bitmap.createBitmap(source, left, top, w, h)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out      = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 95, out)
        val bytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}