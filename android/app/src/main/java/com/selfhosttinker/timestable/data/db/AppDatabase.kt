package com.selfhosttinker.timestable.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.selfhosttinker.timestable.data.db.dao.ClassPresetDao
import com.selfhosttinker.timestable.data.db.dao.GradeEntryDao
import com.selfhosttinker.timestable.data.db.dao.SchoolClassDao
import com.selfhosttinker.timestable.data.db.dao.StudyTaskDao
import com.selfhosttinker.timestable.data.db.dao.SubjectTeacherDao
import com.selfhosttinker.timestable.data.db.dao.TeacherDao
import com.selfhosttinker.timestable.data.db.entity.ClassPresetEntity
import com.selfhosttinker.timestable.data.db.entity.GradeEntryEntity
import com.selfhosttinker.timestable.data.db.entity.SchoolClassEntity
import com.selfhosttinker.timestable.data.db.entity.StudyTaskEntity
import com.selfhosttinker.timestable.data.db.entity.SubjectTeacherCrossRef
import com.selfhosttinker.timestable.data.db.entity.TeacherEntity

@Database(
    entities = [
        SchoolClassEntity::class,
        StudyTaskEntity::class,
        ClassPresetEntity::class,
        GradeEntryEntity::class,
        TeacherEntity::class,
        SubjectTeacherCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolClassDao(): SchoolClassDao
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun classPresetDao(): ClassPresetDao
    abstract fun gradeEntryDao(): GradeEntryDao
    abstract fun teacherDao(): TeacherDao
    abstract fun subjectTeacherDao(): SubjectTeacherDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS grade_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        presetId TEXT,
                        subjectName TEXT NOT NULL,
                        hexColor TEXT NOT NULL,
                        value REAL NOT NULL,
                        weight REAL NOT NULL,
                        dateMs INTEGER NOT NULL,
                        label TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS teachers (id TEXT NOT NULL PRIMARY KEY, firstName TEXT NOT NULL, lastName TEXT NOT NULL)"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS subject_teacher_cross_ref (subjectId TEXT NOT NULL, teacherId TEXT NOT NULL, PRIMARY KEY(subjectId, teacherId))"
                )
            }
        }
    }
}
