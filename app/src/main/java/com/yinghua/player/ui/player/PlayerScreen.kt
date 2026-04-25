package com.yinghua.player.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yinghua.player.data.model.PlayOrientation
import com.yinghua.player.ui.theme.*
import org.videolan.libvlc.interfaces.IVLCVout
import kotlin.math.abs

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    // Apply orientation setting
    LaunchedEffect(state.orientation) {
        activity?.requestedOrientation = when (state.orientation) {
            PlayOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            PlayOrientation.PORTRAIT  -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            PlayOrientation.AUTO      -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    // Keep screen on
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // VLC Surface
        VlcSurface(viewModel = viewModel)

        // Gesture + controls overlay
        PlayerOverlay(
            state = state,
            onBack = onBack,
            onTap = { viewModel.toggleControls() },
            onSeekRelative = { viewModel.seekRelative(it) },
            onSeekTo = { viewModel.seekTo(it) },
            onVolumeChange = { delta ->
                viewModel.setVolume((state.volume + delta).coerceIn(0, 200))
            },
            onBrightnessChange = { delta ->
                viewModel.setBrightness(state.brightness + delta)
            },
            onTogglePlay = { viewModel.togglePlayPause() },
            onToggleLock = { viewModel.toggleLock() },
            onSpeedSelect = { viewModel.setSpeed(it) },
            onAudioTrack = { viewModel.selectAudioTrack(it) },
            onSubTrack = { viewModel.selectSubtitleTrack(it) },
            onSubFile = { uri -> viewModel.addSubtitleFile(uri) },
        )

        // Buffering indicator
        AnimatedVisibility(
            visible = state.isBuffering,
            modifier = Modifier.align(Alignment.Center),
        ) {
            CircularProgressIndicator(color = GreenPrimary, strokeWidth = 3.dp)
        }
    }
}

@Composable
private fun VlcSurface(viewModel: PlayerViewModel) {
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        val mp = viewModel.mediaPlayer ?: return
                        val vout = mp.vlcVout
                        vout.setVideoSurface(holder.surface, holder)
                        viewModel.attachVout(vout)
                    }
                    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, ht: Int) {
                        viewModel.mediaPlayer?.vlcVout?.setWindowSize(w, ht)
                    }
                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        viewModel.mediaPlayer?.let { mp ->
                            viewModel.detachVout(mp.vlcVout)
                        }
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun PlayerOverlay(
    state: PlayerUiState,
    onBack: () -> Unit,
    onTap: () -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onToggleLock: () -> Unit,
    onSpeedSelect: (Float) -> Unit,
    onAudioTrack: (Int) -> Unit,
    onSubTrack: (Int) -> Unit,
    onSubFile: (Uri) -> Unit,
) {
    val subFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onSubFile(it) } }

    // Gesture area
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state.isLocked) {
                if (state.isLocked) {
                    detectTapGestures { onTap() }
                } else {
                    // handled below
                }
            }
    ) {
        if (!state.isLocked) {
            GestureLayer(
                onTap = onTap,
                onDoubleTapLeft  = { onSeekRelative(-10_000L) },
                onDoubleTapRight = { onSeekRelative(10_000L) },
                onSwipeLeft  = { dx -> onSeekRelative(-(dx * 300).toLong()) },
                onSwipeRight = { dx -> onSeekRelative((dx * 300).toLong()) },
                onSwipeUpLeft   = { dy -> onBrightnessChange(dy * 0.5f) },
                onSwipeDownLeft = { dy -> onBrightnessChange(dy * 0.5f) },
                onSwipeUpRight   = { dy -> onVolumeChange((dy * 50).toInt()) },
                onSwipeDownRight = { dy -> onVolumeChange((dy * 50).toInt()) },
            )
        }

        // Controls
        AnimatedVisibility(
            visible = state.showControls || state.isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (state.isLocked) {
                // Only show lock button when locked
                Box(Modifier.fillMaxSize()) {
                    LockButton(
                        isLocked = true,
                        onClick = onToggleLock,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
                    )
                }
            } else {
                PlayerControls(
                    state = state,
                    onBack = onBack,
                    onTogglePlay = onTogglePlay,
                    onToggleLock = onToggleLock,
                    onSeekTo = onSeekTo,
                    onSpeedSelect = onSpeedSelect,
                    onAudioTrack = onAudioTrack,
                    onSubTrack = onSubTrack,
                    onSubFile = { subFileLauncher.launch("*/*") },
                )
            }
        }
    }
}

@Composable
private fun GestureLayer(
    onTap: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit,
    onSwipeLeft: (Float) -> Unit,
    onSwipeRight: (Float) -> Unit,
    onSwipeUpLeft: (Float) -> Unit,
    onSwipeDownLeft: (Float) -> Unit,
    onSwipeUpRight: (Float) -> Unit,
    onSwipeDownRight: (Float) -> Unit,
) {
    var screenWidth by remember { mutableFloatStateOf(1f) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        screenWidth = maxWidth.value

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onDoubleTap = { offset ->
                            if (offset.x < size.width / 2) onDoubleTapLeft()
                            else onDoubleTapRight()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val isLeft = change.position.x < size.width / 2
                        val dx = dragAmount.x / size.width
                        val dy = -dragAmount.y / size.height  // invert: up = positive
                        if (abs(dragAmount.x) > abs(dragAmount.y)) {
                            // Horizontal: seek
                            if (dx < 0) onSwipeLeft(abs(dx)) else onSwipeRight(dx)
                        } else {
                            // Vertical
                            if (isLeft) {
                                if (dy > 0) onSwipeUpLeft(dy) else onSwipeDownLeft(dy)
                            } else {
                                if (dy > 0) onSwipeUpRight(dy) else onSwipeDownRight(dy)
                            }
                        }
                    }
                }
        )
    }
}

@Composable
private fun PlayerControls(
    state: PlayerUiState,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleLock: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSpeedSelect: (Float) -> Unit,
    onAudioTrack: (Int) -> Unit,
    onSubTrack: (Int) -> Unit,
    onSubFile: () -> Unit,
) {
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }
    var showSubMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(0.6f),
                    0.3f to Color.Transparent,
                    0.7f to Color.Transparent,
                    1f to Color.Black.copy(0.7f),
                )
            )
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "返回", tint = Color.White)
            }
            Text(
                text = state.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            // Speed
            TextButton(onClick = { showSpeedMenu = true }) {
                Text("${state.speed}x", color = Color.White, fontSize = 13.sp)
            }
            // Audio
            if (state.audioTracks.isNotEmpty()) {
                IconButton(onClick = { showAudioMenu = true }) {
                    Icon(Icons.Outlined.AudioFile, contentDescription = "音轨", tint = Color.White)
                }
            }
            // Subtitle
            IconButton(onClick = { showSubMenu = true }) {
                Icon(Icons.Outlined.ClosedCaption, contentDescription = "字幕", tint = Color.White)
            }
            // Lock
            LockButton(isLocked = false, onClick = onToggleLock)
        }

        Spacer(Modifier.weight(1f))

        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Progress
            val progress = if (state.duration > 0) state.position.toFloat() / state.duration else 0f
            Slider(
                value = progress,
                onValueChange = { onSeekTo((it * state.duration).toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = GreenPrimary,
                    activeTrackColor = GreenPrimary,
                    inactiveTrackColor = Color.White.copy(0.3f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(formatTime(state.position), color = Color.White.copy(0.8f), fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                // Seek back 10s
                IconButton(onClick = { onSeekTo((state.position - 10_000L).coerceAtLeast(0L)) }) {
                    Icon(Icons.Filled.Replay10, contentDescription = "-10s", tint = Color.White)
                }
                // Play/Pause
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(48.dp)
                        .background(GreenPrimary, CircleShape),
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                // Seek forward 10s
                IconButton(onClick = { onSeekTo((state.position + 10_000L).coerceAtMost(state.duration)) }) {
                    Icon(Icons.Filled.Forward10, contentDescription = "+10s", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text(formatTime(state.duration), color = Color.White.copy(0.8f), fontSize = 12.sp)
            }
        }
    }

    // Speed menu
    if (showSpeedMenu) {
        SpeedMenu(
            current = state.speed,
            onSelect = { onSpeedSelect(it); showSpeedMenu = false },
            onDismiss = { showSpeedMenu = false },
        )
    }

    // Audio track menu
    if (showAudioMenu && state.audioTracks.isNotEmpty()) {
        TrackMenu(
            title = "音频轨道",
            tracks = state.audioTracks,
            selectedId = state.selectedAudioTrack,
            onSelect = { onAudioTrack(it); showAudioMenu = false },
            onDismiss = { showAudioMenu = false },
        )
    }

    // Subtitle menu
    if (showSubMenu) {
        SubtitleMenu(
            tracks = state.subtitleTracks,
            selectedId = state.selectedSubTrack,
            onSelect = { onSubTrack(it); showSubMenu = false },
            onLoadFile = { onSubFile(); showSubMenu = false },
            onDismiss = { showSubMenu = false },
        )
    }
}

@Composable
private fun LockButton(isLocked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp)),
    ) {
        Icon(
            if (isLocked) Icons.Filled.Lock else Icons.Outlined.LockOpen,
            contentDescription = if (isLocked) "解锁" else "锁定",
            tint = Color.White,
        )
    }
}

@Composable
private fun SpeedMenu(current: Float, onSelect: (Float) -> Unit, onDismiss: () -> Unit) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("播放速度") },
        text = {
            Column {
                speeds.forEach { speed ->
                    ListItem(
                        headlineContent = { Text("${speed}x") },
                        trailingContent = {
                            if (speed == current) {
                                Icon(Icons.Filled.Check, null, tint = GreenPrimary)
                            }
                        },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures { onSelect(speed) }
                        },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun TrackMenu(
    title: String,
    tracks: List<TrackInfo>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                tracks.forEach { track ->
                    ListItem(
                        headlineContent = { Text(track.name.ifBlank { "Track ${track.id}" }) },
                        trailingContent = {
                            if (track.id == selectedId) {
                                Icon(Icons.Filled.Check, null, tint = GreenPrimary)
                            }
                        },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures { onSelect(track.id) }
                        },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun SubtitleMenu(
    tracks: List<TrackInfo>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
    onLoadFile: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("字幕") },
        text = {
            Column {
                tracks.forEach { track ->
                    ListItem(
                        headlineContent = { Text(track.name.ifBlank { "字幕 ${track.id}" }) },
                        trailingContent = {
                            if (track.id == selectedId) {
                                Icon(Icons.Filled.Check, null, tint = GreenPrimary)
                            }
                        },
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures { onSelect(track.id) }
                        },
                    )
                }
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("加载外部字幕…") },
                    leadingContent = {
                        Icon(Icons.Outlined.FolderOpen, null, tint = GreenPrimary)
                    },
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures { onLoadFile() }
                    },
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}

private fun formatTime(ms: Long): String {
    val s = ms / 1000
    val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}
