package com.selfhosttinker.timestable.data.db.dao

import androidx.room.*
import com.selfhosttinker.timestable.data.db.entity.SchoolClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolClassDao {

    @Query("SELECT * FROM school_classes ORDER BY startTimeMs ASC")
    fun getAll(): Flow<List<SchoolClassEntity>>

    @Query("SELECT * FROM school_classes WHERE dayOfWeek = :day AND weekIndex = :week ORDER BY startTimeMs ASC")
    fun getByDayAndWeek(day: Int, week: Int): Flow<List<SchoolClassEntity>>

    @Query("SELECT * FROM school_classes WHERE id = :id")
    suspend fun getById(id: String): SchoolClassEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SchoolClassEntity)

    @Update
    suspend fun update(entity: SchoolClassEntity)

    @Delete
    suspend fun delete(entity: SchoolClassEntity)

    @Query("DELETE FROM school_classes")
    suspend fun deleteAll()
}
