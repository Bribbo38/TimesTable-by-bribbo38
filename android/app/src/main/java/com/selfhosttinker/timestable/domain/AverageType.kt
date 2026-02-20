package com.selfhosttinker.timestable.domain

import kotlin.math.*

sealed class AverageType(val rawValue: String, val displayName: String) {

    object Arithmetic     : AverageType("arithmetic",     "Arithmetic Mean")
    object Geometric      : AverageType("geometric",      "Geometric Mean")
    object Harmonic       : AverageType("harmonic",       "Harmonic Mean")
    object Quadratic      : AverageType("quadratic",      "Quadratic Mean (RMS)")
    object Median         : AverageType("median",         "Median")
    object Mode           : AverageType("mode",           "Mode")
    object Trimmed        : AverageType("trimmed",        "Trimmed Mean (10%)")
    object Midrange       : AverageType("midrange",       "Midrange")
    object Cubic          : AverageType("cubic",          "Cubic Mean")
    object Contraharmonic : AverageType("contraharmonic", "Contraharmonic Mean")

    fun compute(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        return when (this) {
            is Arithmetic     -> values.sum() / values.size
            is Geometric      -> exp(values.sumOf { ln(it.coerceAtLeast(1e-10)) } / values.size)
            is Harmonic       -> values.size / values.sumOf { 1.0 / it.coerceAtLeast(1e-10) }
            is Quadratic      -> sqrt(values.sumOf { it * it } / values.size)
            is Median         -> {
                val sorted = values.sorted()
                val mid = sorted.size / 2
                if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0
                else sorted[mid]
            }
            is Mode           -> {
                // Round to 1 decimal before computing frequency
                val rounded = values.map { Math.round(it * 10) / 10.0 }
                rounded.groupBy { it }.maxByOrNull { it.value.size }?.key ?: values.average()
            }
            is Trimmed        -> {
                val sorted = values.sorted()
                val trimCount = (sorted.size * 0.1).toInt().coerceAtLeast(0)
                val trimmed = if (sorted.size > trimCount * 2)
                    sorted.drop(trimCount).dropLast(trimCount)
                else sorted
                if (trimmed.isEmpty()) values.average() else trimmed.average()
            }
            is Midrange       -> (values.min() + values.max()) / 2.0
            is Cubic          -> {
                val cubeMean = values.sumOf { it * it * it } / values.size
                cubeMean.pow(1.0 / 3.0)
            }
            is Contraharmonic -> {
                val sumSquares = values.sumOf { it * it }
                val sumValues  = values.sum()
                if (sumValues == 0.0) 0.0 else sumSquares / sumValues
            }
        }
    }

    companion object {
        val all: List<AverageType> = listOf(
            Arithmetic, Geometric, Harmonic, Quadratic,
            Median, Mode, Trimmed, Midrange, Cubic, Contraharmonic
        )

        fun fromRawValue(raw: String): AverageType =
            all.firstOrNull { it.rawValue == raw } ?: Arithmetic
    }
}
