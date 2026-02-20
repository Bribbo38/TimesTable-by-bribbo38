package com.selfhosttinker.timestable.data.db.dao

import androidx.room.*
import com.selfhosttinker.timestable.data.db.entity.ClassPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassPresetDao {

    @Query("SELECT * FROM class_presets ORDER BY name ASC")
    fun getAll(): Flow<List<ClassPresetEntity>>

    @Query("SELECT * FROM class_presets WHERE id = :id")
    suspend fun getById(id: String): ClassPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ClassPresetEntity)

    @Update
    suspend fun update(entity: ClassPresetEntity)

    @Delete
    suspend fun delete(entity: ClassPresetEntity)

    @Query("DELETE FROM class_presets")
    suspend fun deleteAll()
}
