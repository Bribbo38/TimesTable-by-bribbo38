package com.selfhosttinker.timestable.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String
)
