package com.yinghua.player.ui.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yinghua.player.data.model.DecoderMode
import com.yinghua.player.data.model.PlayOrientation
import com.yinghua.player.data.repository.SettingsRepository
import com.yinghua.player.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IVLCVout
import java.io.File
import javax.inject.Inject

data class PlayerUiState(
    val videoPath: String = "",
    val title: String = "",
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val duration: Long = 0L,
    val position: Long = 0L,
    val volume: Int = 100,
    val brightness: Float = 0.5f,
    val speed: Float = 1.0f,
    val isLocked: Boolean = false,
    val showControls: Boolean = true,
    val audioTracks: List<TrackInfo> = emptyList(),
    val subtitleTracks: List<TrackInfo> = emptyList(),
    val selectedAudioTrack: Int = -1,
    val selectedSubTrack: Int = -1,
    val orientation: PlayOrientation = PlayOrientation.AUTO,
    val isCompleted: Boolean = false,
)

data class TrackInfo(val id: Int, val name: String)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val videoRepository: VideoRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val videoPath: String = checkNotNull(savedStateHandle["videoPath"])

    private val _state = MutableStateFlow(PlayerUiState(videoPath = videoPath))
    val uiState: StateFlow<PlayerUiState> = _state.asStateFlow()

    // VLC
    var libVlc: LibVLC? = null
        private set
    var mediaPlayer: MediaPlayer? = null
        private set

    private var progressJob: kotlinx.coroutines.Job? = null
    private var hideControlsJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            _state.update { it.copy(orientation = settings.defaultOrientation) }
            initVlc(settings.decoderMode)
            loadMedia()
        }
    }

    private fun initVlc(decoderMode: DecoderMode) {
        val options = buildList {
            add("--no-drop-late-frames")
            add("--no-skip-frames")
            add("--avcodec-fast")
            when (decoderMode) {
                DecoderMode.HARDWARE -> add("--codec=mediacodec_ndk,iomx,all")
                DecoderMode.SOFTWARE -> add("--codec=avcodec,all")
                DecoderMode.AUTO     -> { /* VLC default */ }
            }
        }
        libVlc = LibVLC(context, ArrayList(options))
        mediaPlayer = MediaPlayer(libVlc).apply {
            setEventListener { event ->
                handleVlcEvent(event)
            }
        }
    }

    private fun loadMedia() {
        val media = if (videoPath.startsWith("http") || videoPath.startsWith("rtsp") ||
            videoPath.startsWith("rtmp") || videoPath.startsWith("mms")) {
            Media(libVlc, Uri.parse(videoPath))
        } else {
            Media(libVlc, Uri.parse(android.net.Uri.fromFile(java.io.File(videoPath)).toString()))
        }
        val title = videoPath.substringAfterLast("/")
        _state.update { it.copy(title = title) }
        mediaPlayer?.media = media
        media.release()
        mediaPlayer?.play()
    }

    private fun handleVlcEvent(event: MediaPlayer.Event) {
        when (event.type) {
            MediaPlayer.Event.Playing -> {
                _state.update { it.copy(isPlaying = true, isBuffering = false) }
                startProgressTracking()
                updateTracks()
            }
            MediaPlayer.Event.Paused -> {
                _state.update { it.copy(isPlaying = false) }
            }
            MediaPlayer.Event.Stopped -> {
                _state.update { it.copy(isPlaying = false) }
                progressJob?.cancel()
            }
            MediaPlayer.Event.EndReached -> {
                _state.update { it.copy(isPlaying = false, isCompleted = true) }
                progressJob?.cancel()
            }
            MediaPlayer.Event.Buffering -> {
                _state.update { it.copy(isBuffering = event.buffering < 100f) }
            }
            MediaPlayer.Event.LengthChanged -> {
                _state.update { it.copy(duration = event.lengthChanged) }
            }
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                val pos = mediaPlayer?.time ?: 0L
                val dur = mediaPlayer?.length ?: 0L
                _state.update { it.copy(position = pos, duration = dur) }
                delay(500L)
            }
        }
    }

    private fun updateTracks() {
        val mp = mediaPlayer ?: return
        val audioTracks = mp.audioTracks?.map { TrackInfo(it.id, it.name) } ?: emptyList()
        val subTracks = mutableListOf(TrackInfo(-1, "无字幕"))
        subTracks += mp.spuTracks?.map { TrackInfo(it.id, it.name) } ?: emptyList()
        _state.update {
            it.copy(
                audioTracks = audioTracks,
                subtitleTracks = subTracks,
                selectedAudioTrack = mp.audioTrack,
                selectedSubTrack = mp.spuTrack,
            )
        }
    }

    // ── Controls ────────────────────────────────────────────────────────────

    fun togglePlayPause() {
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
        else mediaPlayer?.play()
        scheduleHideControls()
    }

    fun seekTo(ms: Long) {
        mediaPlayer?.time = ms
        _state.update { it.copy(position = ms) }
        scheduleHideControls()
    }

    fun seekRelative(deltaMs: Long) {
        val newPos = ((_state.value.position + deltaMs).coerceIn(0L, _state.value.duration))
        seekTo(newPos)
    }

    fun setVolume(vol: Int) {
        val clamped = vol.coerceIn(0, 200)
        mediaPlayer?.volume = clamped
        _state.update { it.copy(volume = clamped) }
    }

    fun setBrightness(brightness: Float) {
        _state.update { it.copy(brightness = brightness.coerceIn(0f, 1f)) }
    }

    fun setSpeed(speed: Float) {
        mediaPlayer?.rate = speed
        _state.update { it.copy(speed = speed) }
    }

    fun toggleLock() { _state.update { it.copy(isLocked = !it.isLocked) } }

    fun showControls() {
        _state.update { it.copy(showControls = true) }
        scheduleHideControls()
    }

    fun toggleControls() {
        val showing = _state.value.showControls
        _state.update { it.copy(showControls = !showing) }
        if (!showing) scheduleHideControls()
        else hideControlsJob?.cancel()
    }

    private fun scheduleHideControls() {
        hideControlsJob?.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(4_000L)
            _state.update { it.copy(showControls = false) }
        }
    }

    fun selectAudioTrack(id: Int) {
        mediaPlayer?.audioTrack = id
        _state.update { it.copy(selectedAudioTrack = id) }
    }

    fun selectSubtitleTrack(id: Int) {
        mediaPlayer?.spuTrack = id
        _state.update { it.copy(selectedSubTrack = id) }
    }

    fun addSubtitleFile(uri: Uri) {
        mediaPlayer?.addSlave(org.videolan.libvlc.interfaces.IMedia.Slave.Type.Subtitle, uri, true)
    }

    fun attachVout(vout: IVLCVout) {
        vout.attachViews()
    }

    fun detachVout(vout: IVLCVout) {
        vout.detachViews()
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        hideControlsJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        libVlc?.release()
    }
}
