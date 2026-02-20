package com.selfhosttinker.timestable.data.repository

import com.selfhosttinker.timestable.data.db.dao.ClassPresetDao
import com.selfhosttinker.timestable.data.db.entity.ClassPresetEntity
import com.selfhosttinker.timestable.domain.model.ClassPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetRepository @Inject constructor(
    private val dao: ClassPresetDao
) {
    fun getAllPresets(): Flow<List<ClassPreset>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getPresetById(id: String): ClassPreset? =
        dao.getById(id)?.toDomain()

    suspend fun savePreset(preset: ClassPreset) {
        dao.insert(preset.toEntity())
    }

    suspend fun deletePreset(preset: ClassPreset) {
        dao.delete(preset.toEntity())
    }

    suspend fun deleteAllPresets() {
        dao.deleteAll()
    }
}

private fun ClassPresetEntity.toDomain() = ClassPreset(
    id = id,
    name = name,
    room = room,
    teacher = teacher,
    hexColor = hexColor
)

private fun ClassPreset.toEntity() = ClassPresetEntity(
    id = id.ifEmpty { UUID.randomUUID().toString() },
    name = name,
    room = room,
    teacher = teacher,
    hexColor = hexColor
)
