package com.grimorio.rpg.core.designsystem.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String
) {
    MEDIEVAL("medieval", "Pergaminho Medieval", "Dourado rústico & Fantasia Sombria", "📜"),
    CYBERPUNK("cyberpunk", "Cyberpunk Neon", "Ciano elétrico & Magenta sintético", "⚡"),
    COSMIC_HORROR("cosmic", "Horror Cósmico", "AMOLED Negro & Verde Tóxico", "🐙")
}

private val MedievalColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkBackground,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = ArcanePurple,
    onSecondary = TextPrimary,
    secondaryContainer = ArcanePurpleDark,
    onSecondaryContainer = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderRunic
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    onPrimary = CyberpunkBackground,
    primaryContainer = CyberpunkPrimaryDark,
    onPrimaryContainer = CyberpunkPrimaryLight,
    secondary = CyberpunkSecondary,
    onSecondary = CyberpunkBackground,
    secondaryContainer = CyberpunkSecondaryDark,
    onSecondaryContainer = TextPrimary,
    background = CyberpunkBackground,
    onBackground = TextPrimary,
    surface = CyberpunkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberpunkSurfaceVariant,
    onSurfaceVariant = CyberpunkTextMuted,
    outline = CyberpunkBorder
)

private val CosmicHorrorColorScheme = darkColorScheme(
    primary = CosmicPrimary,
    onPrimary = CosmicBackground,
    primaryContainer = CosmicPrimaryDark,
    onPrimaryContainer = CosmicPrimaryLight,
    secondary = CosmicSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = CosmicSecondaryDark,
    onSecondaryContainer = TextPrimary,
    background = CosmicBackground,
    onBackground = TextPrimary,
    surface = CosmicSurface,
    onSurface = TextPrimary,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = CosmicTextMuted,
    outline = CosmicBorder
)

@Composable
fun GrimorioTheme(
    appTheme: AppTheme = AppTheme.MEDIEVAL,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.MEDIEVAL -> MedievalColorScheme
        AppTheme.CYBERPUNK -> CyberpunkColorScheme
        AppTheme.COSMIC_HORROR -> CosmicHorrorColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
