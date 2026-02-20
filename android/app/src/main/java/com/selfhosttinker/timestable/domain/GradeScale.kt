package com.selfhosttinker.timestable.domain

sealed class GradeScale(
    val id: String,
    val displayName: String,
    val min: Double,
    val max: Double,
    /** True when a lower value is better (e.g. German: 1 = best, 6 = worst). */
    val isInverted: Boolean = false,
    val isLetter: Boolean = false
) {
    object OutOf10    : GradeScale("out_of_10",  "Numeric (0 – 10)",          0.0, 10.0)
    object OneTo10    : GradeScale("1_to_10",    "Italian (1 – 10)",          1.0, 10.0)
    object OneTo6     : GradeScale("1_to_6",     "Numeric (1 – 6)",           1.0,  6.0)
    object German     : GradeScale("german",     "German (1 – 6, best = 1)",  1.0,  6.0, isInverted = true)
    object Percentage : GradeScale("percentage", "Percentage (0 – 100 %)",    0.0, 100.0)
    object OutOf20    : GradeScale("out_of_20",  "French (0 – 20)",           0.0, 20.0)
    object OutOf30    : GradeScale("out_of_30",  "University (0 – 30)",       0.0, 30.0)
    object GPA        : GradeScale("gpa",        "GPA (0.0 – 4.0)",           0.0,  4.0)
    object Letter     : GradeScale("letter",     "Letter (A – F)",            0.0,  4.3, isLetter = true)

    /** Returns 0.0–1.0 where 1.0 = best possible performance. */
    fun performance(value: Double): Double = if (isInverted) {
        1.0 - (value - min) / (max - min)
    } else {
        (value - min) / (max - min)
    }.coerceIn(0.0, 1.0)

    /** Human-readable string for this scale's stored Double value. */
    fun displayValue(value: Double): String = when {
        isLetter -> letterLabel(value)
        value == kotlin.math.floor(value) -> value.toLong().toString()
        else -> "%.1f".format(value)
    }

    /**
     * For letter scales: ordered list of (label, internal Double) options.
     * For numeric scales: null (use a text field instead).
     */
    val letterOptions: List<Pair<String, Double>>? =
        if (isLetter) listOf(
            "A+" to 4.3, "A" to 4.0, "A−" to 3.7,
            "B+" to 3.3, "B" to 3.0, "B−" to 2.7,
            "C+" to 2.3, "C" to 2.0, "C−" to 1.7,
            "D"  to 1.0, "F"  to 0.0
        ) else null

    companion object {
        val all: List<GradeScale> = listOf(
            OutOf10, OneTo10, OneTo6, German, Percentage, OutOf20, OutOf30, GPA, Letter
        )
        fun fromId(id: String): GradeScale = all.firstOrNull { it.id == id } ?: OutOf10
    }
}

private fun letterLabel(value: Double): String = when {
    value >= 4.15 -> "A+"
    value >= 3.85 -> "A"
    value >= 3.5  -> "A−"
    value >= 3.15 -> "B+"
    value >= 2.85 -> "B"
    value >= 2.5  -> "B−"
    value >= 2.15 -> "C+"
    value >= 1.85 -> "C"
    value >= 1.5  -> "C−"
    value >= 0.85 -> "D"
    else          -> "F"
}
