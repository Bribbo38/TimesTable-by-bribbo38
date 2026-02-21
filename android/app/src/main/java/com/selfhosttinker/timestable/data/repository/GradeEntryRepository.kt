package com.selfhosttinker.timestable.data.repository

import com.selfhosttinker.timestable.data.db.dao.GradeEntryDao
import com.selfhosttinker.timestable.data.db.entity.GradeEntryEntity
import com.selfhosttinker.timestable.domain.model.GradeEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeEntryRepository @Inject constructor(
    private val dao: GradeEntryDao
) {
    fun getAllEntries(): Flow<List<GradeEntry>> =
        dao.getAllEntries().map { list -> list.map { it.toDomain() } }

    fun getEntriesBySubject(name: String): Flow<List<GradeEntry>> =
        dao.getEntriesBySubject(name).map { list -> list.map { it.toDomain() } }

    suspend fun saveEntry(entry: GradeEntry) {
        dao.insert(entry.toEntity())
    }

    suspend fun deleteEntry(id: String) {
        dao.deleteById(id)
    }
}

private fun GradeEntryEntity.toDomain() = GradeEntry(
    id = id,
    presetId = presetId,
    subjectName = subjectName,
    hexColor = hexColor,
    value = value,
    weight = weight,
    dateMs = dateMs,
    label = label
)

private fun GradeEntry.toEntity() = GradeEntryEntity(
    id = id.ifEmpty { UUID.randomUUID().toString() },
    presetId = presetId,
    subjectName = subjectName,
    hexColor = hexColor,
    value = value,
    weight = weight,
    dateMs = dateMs,
    label = label
)
