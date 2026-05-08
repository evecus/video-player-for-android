package com.yinghua.player.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yinghua.player.BuildConfig
import com.yinghua.player.data.model.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.yinghua.player.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onScan: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val videoCount by viewModel.videoCount.collectAsStateWithLifecycle()
    val folderCount by viewModel.folderCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Scan ──────────────────────────────────────────────────────
            SettingsSectionCard(title = "视频扫描") {
                SettingsInfoRow("已扫描", "$folderCount 个文件夹・$videoCount 个视频")
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("立即扫描") },
                    leadingContent = { Icon(Icons.Outlined.Search, null, tint = GreenPrimary) },
                    trailingContent = {
                        Icon(Icons.Outlined.ChevronRight, null, tint = TextHint)
                    },
                    modifier = Modifier.settingsClickable(onClick = onScan),
                )
            }

            // ── Playback ──────────────────────────────────────────────────
            SettingsSectionCard(title = "播放设置") {
                // Decoder
                SegmentedSetting(
                    icon = Icons.Outlined.Memory,
                    label = "解码方式",
                    options = DecoderMode.entries.map { it.label },
                    selectedIndex = DecoderMode.entries.indexOf(settings.decoderMode),
                    onSelect = { viewModel.setDecoder(DecoderMode.entries[it]) },
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))

                // Orientation
                SegmentedSetting(
                    icon = Icons.Outlined.ScreenRotation,
                    label = "播放方向",
                    options = PlayOrientation.entries.map { it.label },
                    selectedIndex = PlayOrientation.entries.indexOf(settings.defaultOrientation),
                    onSelect = { viewModel.setOrientation(PlayOrientation.entries[it]) },
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))

                // Continuous play
                SwitchSetting(
                    icon = Icons.Outlined.QueuePlayNext,
                    label = "连续播放",
                    description = "播完自动播下一个",
                    checked = settings.continuousPlay,
                    onCheckedChange = { viewModel.setContinuousPlay(it) },
                )
            }

            // ── Display ───────────────────────────────────────────────────
            SettingsSectionCard(title = "显示设置") {
                SwitchSetting(
                    icon = Icons.Outlined.Image,
                    label = "显示视频缩略图",
                    description = "在列表中显示视频封面",
                    checked = settings.showThumbnail,
                    onCheckedChange = { viewModel.setShowThumbnail(it) },
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))

                // Subtitle size slider
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.ClosedCaption, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("字幕大小", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text("${settings.subtitleSize}sp", color = GreenPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Slider(
                        value = settings.subtitleSize.toFloat(),
                        onValueChange = { viewModel.setSubtitleSize(it.toInt()) },
                        valueRange = 10f..32f,
                        steps = 21,
                        colors = SliderDefaults.colors(
                            thumbColor = GreenPrimary,
                            activeTrackColor = GreenPrimary,
                        ),
                    )
                }

                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))

                // Subtitle color
                SegmentedSetting(
                    icon = Icons.Outlined.FormatColorText,
                    label = "字幕颜色",
                    options = SubtitleColor.entries.map { it.label },
                    selectedIndex = SubtitleColor.entries.indexOf(settings.subtitleColor),
                    onSelect = { viewModel.setSubtitleColor(SubtitleColor.entries[it]) },
                )
            }

            // ── About ──────────────────────────────────────────────────────
            SettingsSectionCard(title = "关于") {
                SettingsInfoRow("应用名称", "映画")
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsInfoRow("版本", BuildConfig.VERSION_NAME)
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsInfoRow("播放内核", "VLC libvlc")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSectionCard(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge.copy(color = GreenDark),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Composable
private fun SwitchSetting(
    icon: ImageVector,
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(label, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = description?.let { { Text(it, color = TextHint) } },
        leadingContent = { Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = CardBg, checkedTrackColor = GreenPrimary),
            )
        },
    )
}

@Composable
private fun SegmentedSetting(
    icon: ImageVector,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, text ->
                SegmentedButton(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = GreenLight,
                        activeContentColor = GreenDark,
                    ),
                ) {
                    Text(text, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    ListItem(
        headlineContent = { Text(label, style = MaterialTheme.typography.bodyLarge) },
        trailingContent = { Text(value, color = TextSecondary, style = MaterialTheme.typography.bodyMedium) },
    )
}

private fun Modifier.settingsClickable(onClick: () -> Unit) = this.then(
    Modifier.pointerInput(onClick) {
        detectTapGestures { onClick() }
    }
)
