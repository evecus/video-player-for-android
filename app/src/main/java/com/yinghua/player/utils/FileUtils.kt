package com.yinghua.player.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun renameFile(path: String, newName: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            val ext = file.extension.let { if (it.isNotEmpty()) ".$it" else "" }
            val newNameWithExt = if (newName.contains(".")) newName else "$newName$ext"
            val newFile = File(file.parent, newNameWithExt)
            if (file.renameTo(newFile)) newFile.absolutePath else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun copyFile(sourcePath: String, destDir: String): String? = withContext(Dispatchers.IO) {
        try {
            val source = File(sourcePath)
            val destFolder = File(destDir)
            destFolder.mkdirs()
            val dest = File(destFolder, source.name)
            FileInputStream(source).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
            dest.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun moveFile(sourcePath: String, destDir: String): String? = withContext(Dispatchers.IO) {
        try {
            val source = File(sourcePath)
            val destFolder = File(destDir)
            destFolder.mkdirs()
            val dest = File(destFolder, source.name)
            if (source.renameTo(dest)) {
                dest.absolutePath
            } else {
                // Cross-partition: copy then delete
                FileInputStream(source).use { input ->
                    FileOutputStream(dest).use { output ->
                        input.copyTo(output)
                    }
                }
                source.delete()
                dest.absolutePath
            }
        } catch (e: Exception) {
            null
        }
    }

    fun shareFile(path: String) {
        val file = File(path)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "分享视频").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824L -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024L         -> "%.0f KB".format(bytes / 1_024.0)
            else                    -> "$bytes B"
        }
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}
