package com.yinghua.player.ui.network

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yinghua.player.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    onBack: () -> Unit,
    onPlayUrl: (String) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    // Recent URLs saved in memory for this session
    val recentUrls = remember {
        mutableStateListOf(
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        )
    }

    fun play() {
        val trimmed = url.trim()
        if (trimmed.isNotEmpty()) {
            if (!recentUrls.contains(trimmed)) recentUrls.add(0, trimmed)
            keyboard?.hide()
            onPlayUrl(trimmed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("网络视频") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBackIosNew, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg),
            )
        },
        containerColor = NeutralBg,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // URL input card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "输入视频地址",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        placeholder = { Text("http:// 或 rtsp:// 或 rtmp://…", color = TextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = DividerColor,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { play() }),
                        trailingIcon = {
                            if (url.isNotEmpty()) {
                                IconButton(onClick = { url = "" }) {
                                    Icon(Icons.Filled.Clear, "清除", tint = TextHint)
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Link, null, tint = TextHint)
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = ::play,
                        enabled = url.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("立即播放", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Supported protocols chip row
            Text(
                "支持协议",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("HTTP", "HTTPS", "RTSP", "RTMP", "MMS", "FTP").forEach { proto ->
                    Surface(
                        color = GreenLight,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            proto,
                            style = MaterialTheme.typography.labelSmall.copy(color = GreenDark),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Recent URLs
            if (recentUrls.isNotEmpty()) {
                Text(
                    "最近播放",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(1.dp),
                ) {
                    LazyColumn {
                        items(recentUrls) { recentUrl ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        recentUrl,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Outlined.History,
                                        null,
                                        tint = TextSecondary,
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = { onPlayUrl(recentUrl) }) {
                                        Icon(
                                            Icons.Filled.PlayCircle,
                                            "播放",
                                            tint = GreenPrimary,
                                        )
                                    }
                                },
                                modifier = androidx.compose.ui.Modifier.clickable {
                                    url = recentUrl
                                },
                            )
                            if (recentUrl != recentUrls.last()) {
                                HorizontalDivider(
                                    color = DividerColor,
                                    modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.Modifier.clickable(onClick: () -> Unit) = this.then(
    Modifier.pointerInput(Unit = onClick) {
        androidx.compose.foundation.gestures.detectTapGestures { onClick() }
    }
)
