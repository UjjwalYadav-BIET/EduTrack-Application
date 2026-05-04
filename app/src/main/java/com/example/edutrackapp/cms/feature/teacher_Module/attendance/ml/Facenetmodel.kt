package com.example.edutrackapp.cms.feature.teacher_Module.attendance.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class FaceNetModel(context: Context) {

    companion object {
        private const val MODEL_FILE      = "mobile_face_net.tflite"
        private const val INPUT_SIZE      = 320      // ✅ actual model input size
        private const val EMBEDDING_SIZE  = 128
        private const val BYTES_PER_PIXEL = 1        // ✅ UINT8 = 1 byte per channel
    }

    private val interpreter: Interpreter

    init {
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(loadModelFile(context), options)

        // Confirm actual shape — check Logcat tag "FaceNetModel"
        val inputTensor  = interpreter.getInputTensor(0)
        val outputTensor = interpreter.getOutputTensor(0)
        android.util.Log.d("FaceNetModel", "✅ Input  shape : ${inputTensor.shape().contentToString()}")
        android.util.Log.d("FaceNetModel", "✅ Input  bytes : ${inputTensor.numBytes()}")
        android.util.Log.d("FaceNetModel", "✅ Output shape : ${outputTensor.shape().contentToString()}")
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetFd     = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(assetFd.fileDescriptor)
        val channel     = inputStream.channel
        return channel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFd.startOffset,
            assetFd.declaredLength
        )
    }

    fun getEmbedding(faceBitmap: Bitmap): FloatArray {
        val resized     = Bitmap.createScaledBitmap(faceBitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = bitmapToByteBuffer(resized)

        val outputArray = Array(1) { FloatArray(EMBEDDING_SIZE) }
        interpreter.run(inputBuffer, outputArray)

        return normalise(outputArray[0])
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // ✅ UINT8: 1 byte per channel — total = 320 × 320 × 3 × 1 = 307,200 bytes
        val buffer = ByteBuffer
            .allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * BYTES_PER_PIXEL)
            .apply { order(ByteOrder.nativeOrder()) }

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8)  and 0xFF
            val b =  pixel         and 0xFF
            buffer.put(r.toByte())  // ✅ put byte, not float
            buffer.put(g.toByte())
            buffer.put(b.toByte())
        }
        return buffer
    }

    private fun normalise(v: FloatArray): FloatArray {
        val norm = sqrt(v.map { it * it }.sum())
        return if (norm == 0f) v else FloatArray(v.size) { v[it] / norm }
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float =
        a.zip(b.toTypedArray()).sumOf { (x, y) -> (x * y).toDouble() }.toFloat()

    fun close() = interpreter.close()
}