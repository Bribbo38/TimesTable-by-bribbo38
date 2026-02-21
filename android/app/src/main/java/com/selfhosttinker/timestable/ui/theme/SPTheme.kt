package com.selfhosttinker.timestable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── SP corner radii ────────────────────────────────────────────────────────────
val SPCardRadius     = 12.dp
val SPCardElevation  = 2.dp

// ── SP color palette ──────────────────────────────────────────────────────────
val SPPrimaryBlue    = Color(0xFF1565C0)
val SPPrimaryBlueDark = Color(0xFF90CAF9)
val SPOrange         = Color(0xFFFF6D00)
val SPOrangeDark     = Color(0xFFFFB74D)

// ── SP light color scheme ─────────────────────────────────────────────────────
private val SPLightColors = lightColorScheme(
    primary          = SPPrimaryBlue,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001849),
    secondary        = SPOrange,
    onSecondary      = Color.White,
    background       = Color(0xFFF8F9FA),
    surface          = Color.White,
    surfaceVariant   = Color(0xFFEEF2FF),
    onSurface        = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF636366),
    outline          = Color(0xFFD1D1D6),
    error            = Color(0xFFB00020)
)

// ── SP dark color scheme ──────────────────────────────────────────────────────
private val SPDarkColors = darkColorScheme(
    primary          = SPPrimaryBlueDark,
    onPrimary        = Color(0xFF003366),
    primaryContainer = Color(0xFF00439C),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary        = SPOrangeDark,
    onSecondary      = Color(0xFF462A00),
    background       = Color(0xFF121212),
    surface          = Color(0xFF1E1E1E),
    surfaceVariant   = Color(0xFF2A2A3A),
    onSurface        = Color.White,
    onSurfaceVariant = Color(0xFFB0B0C8),
    outline          = Color(0xFF3A3A5C),
    error            = Color(0xFFCF6679)
)

// ── SP typography (same sizes as AppTypography, default Roboto) ───────────────
val SPTypography = AppTypography

@Composable
fun SPAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) SPDarkColors else SPLightColors
    MaterialTheme(
        colorScheme = colors,
        typography  = SPTypography,
        content     = content
    )
}
