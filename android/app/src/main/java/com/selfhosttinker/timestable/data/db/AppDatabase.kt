package com.selfhosttinker.timestable.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.selfhosttinker.timestable.data.db.dao.ClassPresetDao
import com.selfhosttinker.timestable.data.db.dao.SchoolClassDao
import com.selfhosttinker.timestable.data.db.dao.StudyTaskDao
import com.selfhosttinker.timestable.data.db.entity.ClassPresetEntity
import com.selfhosttinker.timestable.data.db.entity.SchoolClassEntity
import com.selfhosttinker.timestable.data.db.entity.StudyTaskEntity

@Database(
    entities = [
        SchoolClassEntity::class,
        StudyTaskEntity::class,
        ClassPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolClassDao(): SchoolClassDao
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun classPresetDao(): ClassPresetDao
}
