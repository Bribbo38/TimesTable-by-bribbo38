package com.selfhosttinker.timestable.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grade_entries")
data class GradeEntryEntity(
    @PrimaryKey val id: String,
    val presetId: String?,
    val subjectName: String,
    val hexColor: String,
    val value: Double,
    val weight: Double,
    val dateMs: Long,
    val label: String?
)
