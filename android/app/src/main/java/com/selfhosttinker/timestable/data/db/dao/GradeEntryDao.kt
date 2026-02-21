package com.selfhosttinker.timestable.data.db.dao

import androidx.room.*
import com.selfhosttinker.timestable.data.db.entity.GradeEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeEntryDao {

    @Query("SELECT * FROM grade_entries ORDER BY dateMs DESC")
    fun getAllEntries(): Flow<List<GradeEntryEntity>>

    @Query("SELECT * FROM grade_entries WHERE subjectName = :name ORDER BY dateMs DESC")
    fun getEntriesBySubject(name: String): Flow<List<GradeEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GradeEntryEntity)

    @Delete
    suspend fun delete(entry: GradeEntryEntity)

    @Query("DELETE FROM grade_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
