package com.yinghua.player.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yinghua.player.data.model.VideoFolder
import com.yinghua.player.data.repository.SettingsRepository
import com.yinghua.player.data.repository.VideoRepository
import com.yinghua.player.service.ScanService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
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

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == ScanService.ACTION_SCAN_COMPLETE) {
                _isScanning.value = false
                viewModelScope.launch {
                    settingsRepository.updateLastScanTime(System.currentTimeMillis())
                }
            }
        }
    }

    init {
        val filter = IntentFilter(ScanService.ACTION_SCAN_COMPLETE)
        context.registerReceiver(scanReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    fun startScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        val intent = Intent(context, ScanService::class.java)
        context.startForegroundService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        try { context.unregisterReceiver(scanReceiver) } catch (_: Exception) {}
    }
}
