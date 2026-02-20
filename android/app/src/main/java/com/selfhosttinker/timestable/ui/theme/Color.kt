package com.selfhosttinker.timestable.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Palette (mirrors iOS AppTheme.palette) ──────────────────────────────────
val ElectricBlue   = Color(0xFF0A84FF)
val CoralRed       = Color(0xFFFF453A)
val Emerald        = Color(0xFF30D158)
val Amber          = Color(0xFFFFD60A)
val Violet         = Color(0xFFBF5AF2)
val Magenta        = Color(0xFFFF375F)
val Cyan           = Color(0xFF64D2FF)
val Rose           = Color(0xFFFF6482)
val Indigo         = Color(0xFF5E5CE6)
val Lime           = Color(0xFF32D74B)
val Tangerine      = Color(0xFFFF9F0A)
val Mocha          = Color(0xFFAC8E68)

val AppPalette = listOf(
    ElectricBlue, CoralRed, Emerald, Amber,
    Violet, Magenta, Cyan, Rose,
    Indigo, Lime, Tangerine, Mocha
)

// ── Background gradients ─────────────────────────────────────────────────────
val DarkBgStart  = Color(0xFF0C0C1D)
val DarkBgEnd    = Color(0xFF1A1A2E)
val LightBgStart = Color(0xFFF2F2F7)
val LightBgEnd   = Color(0xFFE5E5EA)

// ── Gradient brushes ─────────────────────────────────────────────────────────
val BlueIndigoBrush   = Brush.linearGradient(listOf(ElectricBlue, Indigo))
val GreenEmeraldBrush = Brush.linearGradient(listOf(Emerald, Lime))

// ── Grade color helper ────────────────────────────────────────────────────────
/**
 * Returns green / orange / red based on normalised performance (0.0–1.0).
 * Call with [GradeScale.performance(value)] at the call site.
 */
fun gradeColor(performance: Double): Color = when {
    performance >= 0.75 -> Emerald
    performance >= 0.45 -> Tangerine
    else                -> CoralRed
}

// ── Hex string → Color ───────────────────────────────────────────────────────
fun String.toComposeColor(): Color = try {
    val hex = this.trimStart('#')
    Color(android.graphics.Color.parseColor("#$hex"))
} catch (e: Exception) {
    ElectricBlue
}
