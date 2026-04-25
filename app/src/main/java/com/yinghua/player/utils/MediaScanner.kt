package com.yinghua.player.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.yinghua.player.data.model.VideoFile
import com.yinghua.player.data.model.VideoFolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ScanResult(
    val videos: List<VideoFile>,
    val folders: List<VideoFolder>,
)

@Singleton
class MediaScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun scan(): ScanResult = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoFile>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder,
        )

        cursor?.use { c ->
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val bucketIdCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
            val bucketNameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val widthCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val mimeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val dateAddedCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val dateModCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

            while (c.moveToNext()) {
                val path = c.getString(dataCol) ?: continue
                val file = File(path)
                if (!file.exists()) continue

                val displayName = c.getString(nameCol) ?: file.name
                val folderPath = file.parent ?: continue
                val folderName = c.getString(bucketNameCol) ?: File(folderPath).name

                videos += VideoFile(
                    path = path,
                    name = file.name,
                    displayName = displayName,
                    folderPath = folderPath,
                    folderName = folderName,
                    size = c.getLong(sizeCol),
                    duration = c.getLong(durationCol),
                    width = c.getInt(widthCol),
                    height = c.getInt(heightCol),
                    mimeType = c.getString(mimeCol) ?: "video/*",
                    dateAdded = c.getLong(dateAddedCol),
                    dateModified = c.getLong(dateModCol),
                )
            }
        }

        // Build folder list from grouped videos
        val folderMap = videos.groupBy { it.folderPath }
        val folders = folderMap.map { (folderPath, folderVideos) ->
            VideoFolder(
                path = folderPath,
                name = folderVideos.first().folderName,
                videoCount = folderVideos.size,
                coverPath = folderVideos.first().path,
                totalSize = folderVideos.sumOf { it.size },
                lastModified = folderVideos.maxOf { it.dateModified },
            )
        }.sortedBy { it.name }

        ScanResult(videos = videos, folders = folders)
    }
}
