package com.selfhosttinker.timestable.di

import android.content.Context
import androidx.room.Room
import com.selfhosttinker.timestable.data.db.AppDatabase
import com.selfhosttinker.timestable.data.db.dao.ClassPresetDao
import com.selfhosttinker.timestable.data.db.dao.SchoolClassDao
import com.selfhosttinker.timestable.data.db.dao.StudyTaskDao
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
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSchoolClassDao(db: AppDatabase): SchoolClassDao = db.schoolClassDao()

    @Provides
    fun provideStudyTaskDao(db: AppDatabase): StudyTaskDao = db.studyTaskDao()

    @Provides
    fun provideClassPresetDao(db: AppDatabase): ClassPresetDao = db.classPresetDao()
}
