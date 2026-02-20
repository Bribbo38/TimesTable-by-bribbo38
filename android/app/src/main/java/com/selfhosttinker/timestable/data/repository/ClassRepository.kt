package com.selfhosttinker.timestable.data.repository

import com.selfhosttinker.timestable.data.db.dao.SchoolClassDao
import com.selfhosttinker.timestable.data.db.entity.SchoolClassEntity
import com.selfhosttinker.timestable.domain.model.SchoolClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(
    private val dao: SchoolClassDao
) {
    fun getAllClasses(): Flow<List<SchoolClass>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    fun getClassesByDayAndWeek(day: Int, week: Int): Flow<List<SchoolClass>> =
        dao.getByDayAndWeek(day, week).map { list -> list.map { it.toDomain() } }

    suspend fun getClassById(id: String): SchoolClass? =
        dao.getById(id)?.toDomain()

    suspend fun saveClass(schoolClass: SchoolClass) {
        dao.insert(schoolClass.toEntity())
    }

    suspend fun deleteClass(schoolClass: SchoolClass) {
        dao.delete(schoolClass.toEntity())
    }

    suspend fun deleteAllClasses() {
        dao.deleteAll()
    }
}

private fun SchoolClassEntity.toDomain() = SchoolClass(
    id = id,
    name = name,
    room = room,
    teacher = teacher,
    notes = notes,
    dayOfWeek = dayOfWeek,
    weekIndex = weekIndex,
    startTimeMs = startTimeMs,
    endTimeMs = endTimeMs,
    hexColor = hexColor
)

private fun SchoolClass.toEntity() = SchoolClassEntity(
    id = id.ifEmpty { UUID.randomUUID().toString() },
    name = name,
    room = room,
    teacher = teacher,
    notes = notes,
    dayOfWeek = dayOfWeek,
    weekIndex = weekIndex,
    startTimeMs = startTimeMs,
    endTimeMs = endTimeMs,
    hexColor = hexColor
)
