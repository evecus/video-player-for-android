package com.yinghua.player.ui.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yinghua.player.data.model.VideoFile
import com.yinghua.player.data.repository.SettingsRepository
import com.yinghua.player.data.repository.VideoRepository
import com.yinghua.player.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FolderUiState(
    val folderPath: String = "",
    val folderName: String = "",
    val videos: List<VideoFile> = emptyList(),
    val showThumbnails: Boolean = true,
    val selectedVideo: VideoFile? = null,
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showMoveDialog: Boolean = false,
    val isBusy: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class FolderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val videoRepository: VideoRepository,
    private val settingsRepository: SettingsRepository,
    private val fileUtils: FileUtils,
) : ViewModel() {

    private val folderPath: String = checkNotNull(savedStateHandle["folderPath"])

    private val _extra = MutableStateFlow(
        FolderUiState(folderPath = folderPath)
    )

    val uiState: StateFlow<FolderUiState> = combine(
        videoRepository.getVideosInFolder(folderPath),
        settingsRepository.settingsFlow,
        _extra,
    ) { videos, settings, extra ->
        extra.copy(
            videos = videos,
            showThumbnails = settings.showThumbnail,
            folderName = videos.firstOrNull()?.folderName ?: folderPath.substringAfterLast("/"),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderUiState())

    fun selectVideo(video: VideoFile) {
        _extra.update { it.copy(selectedVideo = video) }
    }

    fun clearSelection() {
        _extra.update { it.copy(selectedVideo = null) }
    }

    fun showRenameDialog() { _extra.update { it.copy(showRenameDialog = true) } }
    fun hideRenameDialog() { _extra.update { it.copy(showRenameDialog = false) } }
    fun showDeleteDialog() { _extra.update { it.copy(showDeleteDialog = true) } }
    fun hideDeleteDialog() { _extra.update { it.copy(showDeleteDialog = false) } }
    fun showMoveDialog()   { _extra.update { it.copy(showMoveDialog = true) } }
    fun hideMoveDialog()   { _extra.update { it.copy(showMoveDialog = false) } }

    fun renameVideo(newName: String) = viewModelScope.launch {
        val video = _extra.value.selectedVideo ?: return@launch
        _extra.update { it.copy(isBusy = true, showRenameDialog = false) }
        val newPath = fileUtils.renameFile(video.path, newName)
        if (newPath != null) {
            videoRepository.deleteVideoByPath(video.path)
            videoRepository.insertVideo(
                video.copy(
                    path = newPath,
                    name = newPath.substringAfterLast("/"),
                    displayName = newName,
                )
            )
            _extra.update { it.copy(isBusy = false, message = "重命名成功", selectedVideo = null) }
        } else {
            _extra.update { it.copy(isBusy = false, message = "重命名失败") }
        }
    }

    fun deleteVideo() = viewModelScope.launch {
        val video = _extra.value.selectedVideo ?: return@launch
        _extra.update { it.copy(isBusy = true, showDeleteDialog = false) }
        val ok = fileUtils.deleteFile(video.path)
        if (ok) {
            videoRepository.deleteVideoByPath(video.path)
            _extra.update { it.copy(isBusy = false, message = "已删除", selectedVideo = null) }
        } else {
            _extra.update { it.copy(isBusy = false, message = "删除失败") }
        }
    }

    fun copyVideo(destDir: String) = viewModelScope.launch {
        val video = _extra.value.selectedVideo ?: return@launch
        _extra.update { it.copy(isBusy = true, showMoveDialog = false) }
        val newPath = fileUtils.copyFile(video.path, destDir)
        if (newPath != null) {
            _extra.update { it.copy(isBusy = false, message = "复制成功", selectedVideo = null) }
        } else {
            _extra.update { it.copy(isBusy = false, message = "复制失败") }
        }
    }

    fun moveVideo(destDir: String) = viewModelScope.launch {
        val video = _extra.value.selectedVideo ?: return@launch
        _extra.update { it.copy(isBusy = true, showMoveDialog = false) }
        val newPath = fileUtils.moveFile(video.path, destDir)
        if (newPath != null) {
            videoRepository.deleteVideoByPath(video.path)
            _extra.update { it.copy(isBusy = false, message = "移动成功", selectedVideo = null) }
        } else {
            _extra.update { it.copy(isBusy = false, message = "移动失败") }
        }
    }

    fun shareVideo() {
        val video = _extra.value.selectedVideo ?: return
        fileUtils.shareFile(video.path)
        _extra.update { it.copy(selectedVideo = null) }
    }

    fun clearMessage() { _extra.update { it.copy(message = null) } }
}
