package com.selfhosttinker.timestable.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_presets")
data class ClassPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val room: String?,
    val teacher: String?,
    val hexColor: String
)
