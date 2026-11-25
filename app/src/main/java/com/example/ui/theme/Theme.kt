package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.ThemePreset

@Immutable
data class MeoMeoPalette(
    val isDark: Boolean,
    val background: Color,
    val navBarBackground: Color,
    val cardGradientTop: Color,
    val cardGradientBottom: Color,
    val cardBorder: Color,
    val cardTopBorder: Color,
    val glassFill: Color,
    val glassTopBorder: Color,
    val glassBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentGlow: Color,
    val accentBright: Color,
    val iconBadgeBg: Color,
    val waveTopPrimary: Color,
    val waveTopSecondary: Color,
    val waveBotPrimary: Color,
    val waveBotSecondary: Color
)

val LocalMeoMeoPalette = staticCompositionLocalOf {
    obsidianPalette()
}

private fun obsidianPalette() = MeoMeoPalette(
    isDark = true,
    background = ObsidianBg,
    navBarBackground = ObsidianNavBg,
    cardGradientTop = ObsidianCardTop,
    cardGradientBottom = ObsidianCardBottom,
    cardBorder = Color.White.copy(alpha = 0.07f),
    cardTopBorder = Color.White.copy(alpha = 0.12f),
    glassFill = Color.White.copy(alpha = 0.08f),
    glassTopBorder = Color.White.copy(alpha = 0.18f),
    glassBorder = Color.White.copy(alpha = 0.07f),
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    accent = PurplePrimary,
    accentGlow = PurpleGlow,
    accentBright = PurpleBright,
    iconBadgeBg = PurpleGlow.copy(alpha = 0.12f),
    waveTopPrimary = WavePurpleTop1,
    waveTopSecondary = WavePurpleTop2,
    waveBotPrimary = WavePurpleBot1,
    waveBotSecondary = WavePurpleBot3
)

private fun midnightPalette() = obsidianPalette().copy(
    background = MidnightBg,
    navBarBackground = Color(0xE6110D1F),
    cardGradientTop = MidnightCardTop,
    cardGradientBottom = MidnightCardBottom,
    waveTopPrimary = Color(0xFF6B21A8),
    waveTopSecondary = Color(0xFF9333EA)
)

private fun amoledPalette() = obsidianPalette().copy(
    background = AmoledBg,
    navBarBackground = Color(0xF0050507),
    cardGradientTop = AmoledCardTop,
    cardGradientBottom = AmoledCardBottom
)

private fun lightPalette() = MeoMeoPalette(
    isDark = false,
    background = LightBg,
    navBarBackground = LightNavBg,
    cardGradientTop = LightCardTop,
    cardGradientBottom = LightCardBottom,
    cardBorder = Color(0xFF7C3AED).copy(alpha = 0.12f),
    cardTopBorder = Color.White,
    glassFill = Color.White.copy(alpha = 0.85f),
    glassTopBorder = Color.White,
    glassBorder = Color(0xFF8B5CF6).copy(alpha = 0.16f),
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    accent = Color(0xFF7C3AED),
    accentGlow = PurplePrimary,
    accentBright = PurpleGlow,
    iconBadgeBg = Color(0xFF8B5CF6).copy(alpha = 0.12f),
    waveTopPrimary = Color(0xFFD8B4FE),
    waveTopSecondary = Color(0xFFC084FC),
    waveBotPrimary = Color(0xFFC4B5FD),
    waveBotSecondary = Color(0xFFA78BFA)
)

@Composable
fun MeoMeoTheme(
    themePreset: ThemePreset = ThemePreset.OBSIDIAN,
    content: @Composable () -> Unit
) {
    val palette = when (themePreset) {
        ThemePreset.OBSIDIAN -> obsidianPalette()
        ThemePreset.MIDNIGHT -> midnightPalette()
        ThemePreset.AMOLED -> amoledPalette()
        ThemePreset.LIGHT -> lightPalette()
    }

    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            secondary = palette.accentBright,
            tertiary = IndigoDeep,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.cardGradientTop,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.cardGradientBottom,
            onSurfaceVariant = palette.textSecondary
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            secondary = palette.accentGlow,
            tertiary = IndigoDeep,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.cardGradientTop,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.cardGradientBottom,
            onSurfaceVariant = palette.textSecondary
        )
    }

    CompositionLocalProvider(LocalMeoMeoPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
