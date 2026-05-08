package com.yinghua.player.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yinghua.player.data.model.VideoFolder
import com.yinghua.player.data.repository.SettingsRepository
import com.yinghua.player.data.repository.VideoRepository
import com.yinghua.player.utils.MediaScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val folders: List<VideoFolder> = emptyList(),
    val isScanning: Boolean = false,
    val lastScanTime: Long = 0L,
    val showThumbnails: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val settingsRepository: SettingsRepository,
    private val mediaScanner: MediaScanner,
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        videoRepository.getAllFolders(),
        settingsRepository.settingsFlow,
        _isScanning,
    ) { folders, settings, scanning ->
        HomeUiState(
            folders = folders,
            isScanning = scanning,
            lastScanTime = settings.lastScanTime,
            showThumbnails = settings.showThumbnail,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun startScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        viewModelScope.launch {
            try {
                val result = mediaScanner.scan()
                videoRepository.replaceAll(result.videos, result.folders)
                settingsRepository.updateLastScanTime(System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }
}
