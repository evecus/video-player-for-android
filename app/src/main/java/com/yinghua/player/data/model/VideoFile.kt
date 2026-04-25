package com.yinghua.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_files")
data class VideoFile(
    @PrimaryKey
    val path: String,
    val name: String,
    val displayName: String,
    val folderPath: String,
    val folderName: String,
    val size: Long,
    val duration: Long,       // milliseconds
    val width: Int = 0,
    val height: Int = 0,
    val mimeType: String = "",
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val thumbnailPath: String? = null,
)
