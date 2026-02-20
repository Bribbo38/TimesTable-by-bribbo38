package com.selfhosttinker.timestable.domain.model

data class StudyTask(
    val id: String = "",
    val title: String = "",
    val detail: String? = null,
    val dueDateMs: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val hexColor: String = "#0A84FF",
    val grade: Double? = null,
    val subjectName: String = "",
    val linkedClassId: String? = null
)
