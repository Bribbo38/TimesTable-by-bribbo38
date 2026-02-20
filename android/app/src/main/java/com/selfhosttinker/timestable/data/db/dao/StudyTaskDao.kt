package com.selfhosttinker.timestable.data.db.dao

import androidx.room.*
import com.selfhosttinker.timestable.data.db.entity.StudyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {

    @Query("SELECT * FROM study_tasks ORDER BY dueDateMs ASC")
    fun getAll(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE linkedClassId = :classId ORDER BY dueDateMs ASC")
    fun getByLinkedClass(classId: String): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE grade IS NOT NULL ORDER BY subjectName ASC")
    fun getAllWithGrades(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE id = :id")
    suspend fun getById(id: String): StudyTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StudyTaskEntity)

    @Update
    suspend fun update(entity: StudyTaskEntity)

    @Delete
    suspend fun delete(entity: StudyTaskEntity)

    @Query("DELETE FROM study_tasks")
    suspend fun deleteAll()
}
