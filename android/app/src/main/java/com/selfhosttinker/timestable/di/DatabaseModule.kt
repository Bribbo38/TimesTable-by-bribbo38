package com.selfhosttinker.timestable.di

import android.content.Context
import androidx.room.Room
import com.selfhosttinker.timestable.data.db.AppDatabase
import com.selfhosttinker.timestable.data.db.dao.ClassPresetDao
import com.selfhosttinker.timestable.data.db.dao.GradeEntryDao
import com.selfhosttinker.timestable.data.db.dao.SchoolClassDao
import com.selfhosttinker.timestable.data.db.dao.StudyTaskDao
import com.selfhosttinker.timestable.data.db.dao.SubjectTeacherDao
import com.selfhosttinker.timestable.data.db.dao.TeacherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "timestable.db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()

    @Provides
    fun provideSchoolClassDao(db: AppDatabase): SchoolClassDao = db.schoolClassDao()

    @Provides
    fun provideStudyTaskDao(db: AppDatabase): StudyTaskDao = db.studyTaskDao()

    @Provides
    fun provideClassPresetDao(db: AppDatabase): ClassPresetDao = db.classPresetDao()

    @Provides
    fun provideGradeEntryDao(db: AppDatabase): GradeEntryDao = db.gradeEntryDao()

    @Provides
    fun provideTeacherDao(db: AppDatabase): TeacherDao = db.teacherDao()

    @Provides
    fun provideSubjectTeacherDao(db: AppDatabase): SubjectTeacherDao = db.subjectTeacherDao()
}
