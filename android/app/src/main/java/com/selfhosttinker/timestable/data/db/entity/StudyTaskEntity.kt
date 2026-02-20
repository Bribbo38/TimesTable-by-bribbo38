package com.selfhosttinker.timestable.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_tasks",
    foreignKeys = [ForeignKey(
        entity = SchoolClassEntity::class,
        parentColumns = ["id"],
        childColumns = ["linkedClassId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("linkedClassId")]
)
data class StudyTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val detail: String?,
    val dueDateMs: Long,
    val isCompleted: Boolean,
    val hexColor: String,
    val grade: Double?,        // null until entered
    val subjectName: String,   // copy of class name for grade grouping
    val linkedClassId: String? // FK to SchoolClassEntity.id
)
