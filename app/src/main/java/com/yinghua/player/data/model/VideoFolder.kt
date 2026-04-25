package com.yinghua.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_folders")
data class VideoFolder(
    @PrimaryKey
    val path: String,
    val name: String,
    val videoCount: Int = 0,
    val coverPath: String? = null,     // path of first video for cover thumb
    val totalSize: Long = 0L,
    val lastModified: Long = 0L,
)
