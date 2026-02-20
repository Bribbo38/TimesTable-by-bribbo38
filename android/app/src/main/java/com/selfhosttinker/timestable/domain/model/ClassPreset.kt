package com.selfhosttinker.timestable.domain.model

data class ClassPreset(
    val id: String = "",
    val name: String = "",
    val room: String? = null,
    val teacher: String? = null,
    val hexColor: String = "#0A84FF"
)
