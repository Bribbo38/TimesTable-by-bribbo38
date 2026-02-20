package com.selfhosttinker.timestable.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_classes")
data class SchoolClassEntity(
    @PrimaryKey val id: String,
    val name: String,
    val room: String?,
    val teacher: String?,
    val notes: String?,
    val dayOfWeek: Int,      // 1=Mon … 7=Sun
    val weekIndex: Int,      // 1–4
    val startTimeMs: Long,   // epoch millis (only time-of-day portion used)
    val endTimeMs: Long,
    val hexColor: String     // e.g. "#0A84FF"
)
