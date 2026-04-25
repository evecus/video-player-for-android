package com.yinghua.player.data.repository

import com.yinghua.player.data.db.VideoDao
import com.yinghua.player.data.model.VideoFile
import com.yinghua.player.data.model.VideoFolder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val dao: VideoDao
) {
    fun getAllFolders(): Flow<List<VideoFolder>> = dao.getAllFolders()

    fun getVideosInFolder(folderPath: String): Flow<List<VideoFile>> =
        dao.getVideosInFolder(folderPath)

    fun searchVideos(query: String): Flow<List<VideoFile>> =
        dao.searchVideos(query)

    suspend fun getVideoByPath(path: String): VideoFile? =
        dao.getVideoByPath(path)

    suspend fun replaceAll(videos: List<VideoFile>, folders: List<VideoFolder>) =
        dao.replaceAll(videos, folders)

    suspend fun deleteVideo(video: VideoFile) = dao.deleteVideo(video)

    suspend fun deleteVideoByPath(path: String) = dao.deleteVideoByPath(path)

    suspend fun insertVideo(video: VideoFile) = dao.insertVideo(video)

    suspend fun getVideoCount(): Int = dao.getVideoCount()

    suspend fun getFolderCount(): Int = dao.getFolderCount()
}
