package com.yinghua.player.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import coil.decode.VideoFrameDecoder
import com.yinghua.player.data.model.VideoFolder
import com.yinghua.player.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFolderClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HomeTopBar(
                isScanning = state.isScanning,
                onSearchClick = onSearchClick,
                onNetworkClick = onNetworkClick,
                onSettingsClick = onSettingsClick,
                onScanClick = { viewModel.startScan() },
            )
        },
        containerColor = NeutralBg,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.folders.isEmpty() && !state.isScanning) {
                EmptyState(onScanClick = { viewModel.startScan() })
            } else {
                FolderGrid(
                    folders = state.folders,
                    showThumbnails = state.showThumbnails,
                    lastScanTime = state.lastScanTime,
                    onFolderClick = onFolderClick,
                )
            }

            // Scanning overlay banner
            AnimatedVisibility(
                visible = state.isScanning,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                ScanningBanner()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    isScanning: Boolean,
    onSearchClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onScanClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gradient brand name
                Text(
                    text = "映",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(GreenStart, GreenEnd)
                        ),
                        fontWeight = FontWeight.ExtraBold,
                    ),
                )
                Text(
                    text = "画",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Outlined.Search, contentDescription = "搜索", tint = TextSecondary)
            }
            IconButton(onClick = onNetworkClick) {
                Icon(Icons.Outlined.WifiFind, contentDescription = "网络视频", tint = TextSecondary)
            }
            IconButton(onClick = onScanClick, enabled = !isScanning) {
                Icon(
                    if (isScanning) Icons.Filled.Sync else Icons.Outlined.Refresh,
                    contentDescription = "扫描",
                    tint = if (isScanning) GreenPrimary else TextSecondary,
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Outlined.Settings, contentDescription = "设置", tint = TextSecondary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CardBg,
        ),
    )
}

@Composable
private fun FolderGrid(
    folders: List<VideoFolder>,
    showThumbnails: Boolean,
    lastScanTime: Long,
    onFolderClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (lastScanTime > 0L) {
            item(span = { GridItemSpan(2) }) {
                ScanSummaryChip(
                    folderCount = folders.size,
                    lastScanTime = lastScanTime,
                )
            }
        }

        items(folders, key = { it.path }) { folder ->
            FolderCard(
                folder = folder,
                showThumbnail = showThumbnails,
                onClick = { onFolderClick(folder.path) },
            )
        }
    }
}

@Composable
private fun ScanSummaryChip(folderCount: Int, lastScanTime: Long) {
    val timeStr = remember(lastScanTime) {
        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(lastScanTime))
    }
    Surface(
        color = GreenLight,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "共 $folderCount 个文件夹 · 最后扫描：$timeStr",
                style = MaterialTheme.typography.bodySmall.copy(color = GreenDark),
            )
        }
    }
}

@Composable
private fun FolderCard(
    folder: VideoFolder,
    showThumbnail: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
    ) {
        Column {
            // Thumbnail area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFEEF4F2))
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (showThumbnail && folder.coverPath != null) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(folder.coverPath))
                            .decoderFactory { result, options, _ ->
                                VideoFrameDecoder(result.source, options)
                            }
                            .size(Size(400, 400))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.25f),
                                    )
                                )
                            )
                    )
                } else {
                    Icon(
                        Icons.Filled.VideoLibrary,
                        contentDescription = null,
                        tint = GreenPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp),
                    )
                }

                // Video count badge
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                ) {
                    Text(
                        text = "${folder.videoCount} 个",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            // Folder name + meta
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = truncatePath(folder.path),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onScanClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.VideoLibrary,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GreenPrimary.copy(alpha = 0.3f),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "暂无视频文件夹",
            style = MaterialTheme.typography.headlineSmall,
            color = TextSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "点击下方按钮扫描设备中的视频文件",
            style = MaterialTheme.typography.bodyMedium,
            color = TextHint,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onScanClick,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("扫描视频", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ScanningBanner() {
    Surface(
        color = GreenPrimary,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "正在扫描视频文件…",
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun truncatePath(path: String): String {
    val parts = path.split("/").filter { it.isNotEmpty() }
    return if (parts.size <= 3) "/$path"
    else "…/" + parts.takeLast(2).joinToString("/")
}
