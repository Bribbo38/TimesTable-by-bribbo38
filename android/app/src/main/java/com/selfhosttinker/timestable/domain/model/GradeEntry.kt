package com.selfhosttinker.timestable.domain.model

data class GradeEntry(
    val id: String = "",
    val presetId: String? = null,
    val subjectName: String = "",
    val hexColor: String = "#1565C0",
    val value: Double = 0.0,
    val weight: Double = 1.0,
    val dateMs: Long = System.currentTimeMillis(),
    val label: String? = null
)
