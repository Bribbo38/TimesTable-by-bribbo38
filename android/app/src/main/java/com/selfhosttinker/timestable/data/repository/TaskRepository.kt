package com.selfhosttinker.timestable.data.repository

import com.selfhosttinker.timestable.data.db.dao.StudyTaskDao
import com.selfhosttinker.timestable.data.db.entity.StudyTaskEntity
import com.selfhosttinker.timestable.domain.model.StudyTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val dao: StudyTaskDao
) {
    fun getAllTasks(): Flow<List<StudyTask>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    fun getTasksByClass(classId: String): Flow<List<StudyTask>> =
        dao.getByLinkedClass(classId).map { list -> list.map { it.toDomain() } }

    fun getTasksWithGrades(): Flow<List<StudyTask>> =
        dao.getAllWithGrades().map { list -> list.map { it.toDomain() } }

    suspend fun getTaskById(id: String): StudyTask? =
        dao.getById(id)?.toDomain()

    suspend fun saveTask(task: StudyTask) {
        dao.insert(task.toEntity())
    }

    suspend fun deleteTask(task: StudyTask) {
        dao.delete(task.toEntity())
    }

    suspend fun deleteAllTasks() {
        dao.deleteAll()
    }
}

private fun StudyTaskEntity.toDomain() = StudyTask(
    id = id,
    title = title,
    detail = detail,
    dueDateMs = dueDateMs,
    isCompleted = isCompleted,
    hexColor = hexColor,
    grade = grade,
    subjectName = subjectName,
    linkedClassId = linkedClassId
)

private fun StudyTask.toEntity() = StudyTaskEntity(
    id = id.ifEmpty { UUID.randomUUID().toString() },
    title = title,
    detail = detail,
    dueDateMs = dueDateMs,
    isCompleted = isCompleted,
    hexColor = hexColor,
    grade = grade,
    subjectName = subjectName,
    linkedClassId = linkedClassId
)
