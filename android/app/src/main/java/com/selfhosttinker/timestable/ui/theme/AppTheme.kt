package com.selfhosttinker.timestable.ui.theme

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ── Corner radii ──────────────────────────────────────────────────────────────
val CardRadius  = 16.dp
val ChipRadius  = 10.dp
val PillRadius  = 20.dp

// ── Animation specs ───────────────────────────────────────────────────────────
val BouncySpec   = spring<Float>(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
val SmoothSpec   = spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
val QuickSpec    = tween<Float>(durationMillis = 200, easing = FastOutLinearInEasing)

// ── Custom dark/light color schemes ──────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = ElectricBlue,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF1A3A5C),
    secondary        = Indigo,
    onSecondary      = Color.White,
    background       = DarkBgStart,
    surface          = Color(0xFF14142A),
    onSurface        = Color.White,
    surfaceVariant   = Color(0xFF1E1E3A),
    onSurfaceVariant = Color(0xFFB0B0C8),
    outline          = Color(0xFF3A3A5C),
    error            = CoralRed
)

private val LightColorScheme = lightColorScheme(
    primary          = ElectricBlue,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD6EAFF),
    secondary        = Indigo,
    onSecondary      = Color.White,
    background       = LightBgStart,
    surface          = Color.White,
    onSurface        = Color(0xFF1C1C1E),
    surfaceVariant   = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF636366),
    outline          = Color(0xFFD1D1D6),
    error            = CoralRed
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
