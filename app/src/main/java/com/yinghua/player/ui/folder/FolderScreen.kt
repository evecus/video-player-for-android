package com.yinghua.player.ui.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.yinghua.player.data.model.VideoFile
import com.yinghua.player.ui.theme.*
import com.yinghua.player.utils.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit,
    viewModel: FolderViewModel = hiltViewModel(),
    fileUtils: FileUtils? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.folderName, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${state.videos.size} 个视频",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NeutralBg,
    ) { padding ->
        if (state.videos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("该文件夹暂无视频", color = TextHint)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 12.dp, end = 12.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.videos, key = { it.path }) { video ->
                    VideoItem(
                        video = video,
                        showThumbnail = state.showThumbnails,
                        onClick = { onVideoClick(video.path) },
                        onLongClick = { viewModel.selectVideo(video) },
                    )
                }
            }
        }
    }

    // Bottom sheet for file operations
    state.selectedVideo?.let { video ->
        VideoOptionsSheet(
            video = video,
            onDismiss = { viewModel.clearSelection() },
            onRename = { viewModel.showRenameDialog() },
            onDelete = { viewModel.showDeleteDialog() },
            onShare  = { viewModel.shareVideo() },
            onCopy   = { viewModel.showMoveDialog() },
            onMove   = { viewModel.showMoveDialog() },
        )
    }

    if (state.showRenameDialog) {
        RenameDialog(
            currentName = state.selectedVideo?.displayName ?: "",
            onConfirm = { viewModel.renameVideo(it) },
            onDismiss = { viewModel.hideRenameDialog() },
        )
    }

    if (state.showDeleteDialog) {
        DeleteDialog(
            fileName = state.selectedVideo?.displayName ?: "",
            onConfirm = { viewModel.deleteVideo() },
            onDismiss = { viewModel.hideDeleteDialog() },
        )
    }

    if (state.showMoveDialog) {
        MoveDialog(
            onCopy = { dest -> viewModel.copyVideo(dest) },
            onMove = { dest -> viewModel.moveVideo(dest) },
            onDismiss = { viewModel.hideMoveDialog() },
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun VideoItem(
    video: VideoFile,
    showThumbnail: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE8F5F1)),
                contentAlignment = Alignment.Center,
            ) {
                if (showThumbnail) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(video.path))
                            .decoderFactory { result, options, _ ->
                                VideoFrameDecoder(result.source, options)
                            }
                            .size(Size(200, 128))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = GreenPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp),
                    )
                }
                // Duration badge
                Surface(
                    color = Color.Black.copy(0.6f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                ) {
                    Text(
                        text = formatDuration(video.duration),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(formatSize(video.size))
                    if (video.width > 0 && video.height > 0) {
                        InfoChip("${video.width}×${video.height}")
                    }
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        color = GreenLight,
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(color = GreenDark),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoOptionsSheet(
    video: VideoFile,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = video.displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            HorizontalDivider(color = DividerColor)
            listOf(
                Triple(Icons.Outlined.DriveFileRenameOutline, "重命名", onRename),
                Triple(Icons.Outlined.Share, "分享", onShare),
                Triple(Icons.Outlined.FileCopy, "复制到…", onCopy),
                Triple(Icons.Outlined.DriveFileMoveOutline, "移动到…", onMove),
                Triple(Icons.Outlined.DeleteOutline, "删除", onDelete),
            ).forEach { (icon, label, action) ->
                ListItem(
                    headlineContent = {
                        Text(
                            label,
                            color = if (label == "删除") ErrorRed else TextPrimary,
                        )
                    },
                    leadingContent = {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (label == "删除") ErrorRed else TextSecondary,
                        )
                    },
                    modifier = Modifier.combinedClickable(onClick = { onDismiss(); action() }),
                )
            }
        }
    }
}

@Composable
private fun RenameDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(currentName.substringBeforeLast(".")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("新名称") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text) }) { Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun DeleteDialog(fileName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = ErrorRed) },
        title = { Text("确认删除") },
        text = { Text("确定要删除 "$fileName" 吗？删除后无法恢复。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", color = ErrorRed, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun MoveDialog(onCopy: (String) -> Unit, onMove: (String) -> Unit, onDismiss: () -> Unit) {
    var path by remember { mutableStateOf("") }
    var isCopy by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCopy) "复制到" else "移动到") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isCopy, onClick = { isCopy = true })
                    Text("复制")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = !isCopy, onClick = { isCopy = false })
                    Text("移动")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("目标路径") },
                    placeholder = { Text("/sdcard/目标文件夹") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (path.isNotBlank()) {
                    if (isCopy) onCopy(path) else onMove(path)
                }
            }) { Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

private fun formatDuration(ms: Long): String {
    val s = ms / 1000
    val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L     -> "%.0f MB".format(bytes / 1_048_576.0)
    else                    -> "%.0f KB".format(bytes / 1_024.0)
}
