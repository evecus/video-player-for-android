package com.yinghua.player.data.model

data class AppSettings(
    val decoderMode: DecoderMode = DecoderMode.AUTO,
    val defaultOrientation: PlayOrientation = PlayOrientation.AUTO,
    val showThumbnail: Boolean = true,
    val continuousPlay: Boolean = true,
    val subtitleSize: Int = 16,
    val subtitleColor: SubtitleColor = SubtitleColor.WHITE,
    val lastScanTime: Long = 0L,
)

enum class DecoderMode(val label: String) {
    AUTO("自动"),
    HARDWARE("硬件解码"),
    SOFTWARE("软件解码"),
}

enum class PlayOrientation(val label: String) {
    AUTO("跟随传感器"),
    LANDSCAPE("横屏"),
    PORTRAIT("竖屏"),
}

enum class SubtitleColor(val label: String) {
    WHITE("白色"),
    YELLOW("黄色"),
    GREEN("绿色"),
    CYAN("青色"),
}
