package com.yinghua.player.data.db

import androidx.room.*
import com.yinghua.player.data.model.VideoFile
import com.yinghua.player.data.model.VideoFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    // ── VideoFile ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM video_files ORDER BY dateAdded DESC")
    fun getAllVideos(): Flow<List<VideoFile>>

    @Query("SELECT * FROM video_files WHERE folderPath = :folderPath ORDER BY displayName ASC")
    fun getVideosInFolder(folderPath: String): Flow<List<VideoFile>>

    @Query("SELECT * FROM video_files WHERE displayName LIKE '%' || :query || '%' ORDER BY displayName ASC")
    fun searchVideos(query: String): Flow<List<VideoFile>>

    @Query("SELECT * FROM video_files WHERE path = :path LIMIT 1")
    suspend fun getVideoByPath(path: String): VideoFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoFile>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoFile)

    @Delete
    suspend fun deleteVideo(video: VideoFile)

    @Query("DELETE FROM video_files WHERE path = :path")
    suspend fun deleteVideoByPath(path: String)

    @Query("DELETE FROM video_files")
    suspend fun deleteAllVideos()

    @Query("SELECT COUNT(*) FROM video_files")
    suspend fun getVideoCount(): Int

    // ── VideoFolder ────────────────────────────────────────────────────────

    @Query("SELECT * FROM video_folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<VideoFolder>>

    @Query("SELECT * FROM video_folders WHERE path = :path LIMIT 1")
    suspend fun getFolderByPath(path: String): VideoFolder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<VideoFolder>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: VideoFolder)

    @Delete
    suspend fun deleteFolder(folder: VideoFolder)

    @Query("DELETE FROM video_folders")
    suspend fun deleteAllFolders()

    @Query("SELECT COUNT(*) FROM video_folders")
    suspend fun getFolderCount(): Int

    // ── Combined ───────────────────────────────────────────────────────────

    @Transaction
    suspend fun replaceAll(videos: List<VideoFile>, folders: List<VideoFolder>) {
        deleteAllVideos()
        deleteAllFolders()
        insertVideos(videos)
        insertFolders(folders)
    }
}
