package com.selfhosttinker.timestable.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "subject_teacher_cross_ref",
    primaryKeys = ["subjectId", "teacherId"]
)
data class SubjectTeacherCrossRef(
    val subjectId: String,
    val teacherId: String
)
