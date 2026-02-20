package com.selfhosttinker.timestable.domain.model

data class SchoolClass(
    val id: String = "",
    val name: String = "",
    val room: String? = null,
    val teacher: String? = null,
    val notes: String? = null,
    val dayOfWeek: Int = 1,   // 1=Mon … 7=Sun
    val weekIndex: Int = 1,   // 1–4
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = 0L,
    val hexColor: String = "#0A84FF"
)
