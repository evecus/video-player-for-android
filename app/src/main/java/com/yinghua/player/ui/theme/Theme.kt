package com.yinghua.player.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand palette ──────────────────────────────────────────────────────────
val GreenStart   = Color(0xFF3ECFAA)   // teal-green (icon left)
val GreenEnd     = Color(0xFF52D98C)   // lighter green (icon right)
val GreenPrimary = Color(0xFF2CC090)   // main action colour
val GreenDark    = Color(0xFF1A9E77)   // pressed / dark accent
val GreenLight   = Color(0xFFE6FAF5)   // surface tint / chip background

val NeutralBg    = Color(0xFFF8FAFB)   // page background
val CardBg       = Color(0xFFFFFFFF)
val DividerColor = Color(0xFFEEF0F2)
val TextPrimary  = Color(0xFF1A1F2E)
val TextSecondary= Color(0xFF6B7280)
val TextHint     = Color(0xFFADB5BD)

val ErrorRed     = Color(0xFFEF4444)

// ── Light colour scheme ───────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary          = GreenPrimary,
    onPrimary        = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,
    secondary        = GreenEnd,
    onSecondary      = Color.White,
    secondaryContainer = Color(0xFFDCFCEF),
    onSecondaryContainer = GreenDark,
    background       = NeutralBg,
    onBackground     = TextPrimary,
    surface          = CardBg,
    onSurface        = TextPrimary,
    surfaceVariant   = Color(0xFFF0F4F3),
    onSurfaceVariant = TextSecondary,
    outline          = DividerColor,
    error            = ErrorRed,
    onError          = Color.White,
)

// ── Typography ────────────────────────────────────────────────────────────
val YingHuaTypography = Typography(
    // App bar / section titles
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        color = TextPrimary,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = TextPrimary,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TextPrimary,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextSecondary,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextHint,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
    ),
)

// ── Theme entry point ──────────────────────────────────────────────────────
@Composable
fun YingHuaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = YingHuaTypography,
        content     = content,
    )
}
