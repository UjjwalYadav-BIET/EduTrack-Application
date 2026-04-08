package com.example.edutrackapp.cms.feature.notices

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Decodes Base64 attachment and opens it.
 * Uses MediaStore on Android 10+ and direct Downloads write on older versions.
 * No FileProvider required.
 */
suspend fun openAttachment(
    context: Context,
    base64Data: String,
    fileName: String,
    mimeType: String
) {
    try {
        if (base64Data.isBlank()) {
            Toast.makeText(context, "No attachment found.", Toast.LENGTH_SHORT).show()
            return
        }

        // Decode on IO thread
        val bytes = withContext(Dispatchers.IO) {
            Base64.decode(base64Data, Base64.DEFAULT)
        }

        if (bytes.isEmpty()) {
            Toast.makeText(context, "Attachment is empty.", Toast.LENGTH_SHORT).show()
            return
        }

        val safeFileName = fileName.ifBlank { "notice_attachment" }
        val safeMime     = mimeType.ifBlank { getMimeFromFileName(safeFileName) }

        val contentUri: Uri = withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ — use MediaStore Downloads
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, safeFileName)
                    put(MediaStore.Downloads.MIME_TYPE, safeMime)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw Exception("Failed to create MediaStore entry.")

                resolver.openOutputStream(uri)?.use { it.write(bytes) }

                // Mark as ready
                val updateValues = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                resolver.update(uri, updateValues, null, null)
                uri
            } else {
                // Android 9 and below — write directly to Downloads folder
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                downloadsDir.mkdirs()
                val file = File(downloadsDir, safeFileName)
                FileOutputStream(file).use { it.write(bytes) }

                // Scan the file so it appears in MediaStore
                Uri.fromFile(file)
            }
        }

        Toast.makeText(context, "Saved to Downloads: $safeFileName", Toast.LENGTH_SHORT).show()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, safeMime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // No app to open it — just inform user it's saved
            Toast.makeText(
                context,
                "Saved to Downloads/$safeFileName — open from your file manager.",
                Toast.LENGTH_LONG
            ).show()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Could not open file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun getMimeFromFileName(fileName: String): String {
    return when (fileName.substringAfterLast('.', "").lowercase()) {
        "pdf"         -> "application/pdf"
        "doc"         -> "application/msword"
        "docx"        -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls"         -> "application/vnd.ms-excel"
        "xlsx"        -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt"         -> "application/vnd.ms-powerpoint"
        "pptx"        -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "jpg", "jpeg" -> "image/jpeg"
        "png"         -> "image/png"
        "gif"         -> "image/gif"
        "mp4"         -> "video/mp4"
        "mp3"         -> "audio/mpeg"
        "txt"         -> "text/plain"
        "zip"         -> "application/zip"
        else          -> "application/octet-stream"
    }
}