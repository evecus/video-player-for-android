package com.yinghua.player.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yinghua.player.data.model.*
import com.yinghua.player.data.repository.SettingsRepository
import com.yinghua.player.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val videoRepository: VideoRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _videoCount  = MutableStateFlow(0)
    private val _folderCount = MutableStateFlow(0)
    val videoCount: StateFlow<Int>  = _videoCount.asStateFlow()
    val folderCount: StateFlow<Int> = _folderCount.asStateFlow()

    init {
        viewModelScope.launch {
            _videoCount.value  = videoRepository.getVideoCount()
            _folderCount.value = videoRepository.getFolderCount()
        }
    }

    fun setDecoder(mode: DecoderMode) = viewModelScope.launch {
        settingsRepository.updateDecoderMode(mode)
    }
    fun setOrientation(o: PlayOrientation) = viewModelScope.launch {
        settingsRepository.updateOrientation(o)
    }
    fun setShowThumbnail(v: Boolean) = viewModelScope.launch {
        settingsRepository.updateShowThumbnail(v)
    }
    fun setContinuousPlay(v: Boolean) = viewModelScope.launch {
        settingsRepository.updateContinuousPlay(v)
    }
    fun setSubtitleSize(v: Int) = viewModelScope.launch {
        settingsRepository.updateSubtitleSize(v)
    }
    fun setSubtitleColor(c: SubtitleColor) = viewModelScope.launch {
        settingsRepository.updateSubtitleColor(c)
    }
}
