package com.selfhosttinker.timestable.data.repository

import com.selfhosttinker.timestable.data.db.dao.SubjectTeacherDao
import com.selfhosttinker.timestable.data.db.dao.TeacherDao
import com.selfhosttinker.timestable.data.db.entity.ClassPresetEntity
import com.selfhosttinker.timestable.data.db.entity.SubjectTeacherCrossRef
import com.selfhosttinker.timestable.data.db.entity.TeacherEntity
import com.selfhosttinker.timestable.domain.model.ClassPreset
import com.selfhosttinker.timestable.domain.model.Teacher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepository @Inject constructor(
    private val teacherDao: TeacherDao,
    private val crossRefDao: SubjectTeacherDao
) {
    fun getAllTeachers(): Flow<List<Teacher>> =
        teacherDao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun saveTeacher(teacher: Teacher) {
        teacherDao.insert(teacher.toEntity())
    }

    suspend fun deleteTeacher(id: String) {
        crossRefDao.clearSubjectsForTeacher(id)
        teacherDao.deleteById(id)
    }

    fun getTeachersForSubject(subjectId: String): Flow<List<Teacher>> =
        crossRefDao.getTeachersForSubject(subjectId).map { list -> list.map { it.toDomain() } }

    fun getSubjectsForTeacher(teacherId: String): Flow<List<ClassPreset>> =
        crossRefDao.getSubjectsForTeacher(teacherId).map { list -> list.map { it.toDomain() } }

    suspend fun setTeachersForSubject(subjectId: String, teacherIds: List<String>) {
        crossRefDao.clearTeachersForSubject(subjectId)
        teacherIds.forEach { teacherId ->
            crossRefDao.insert(SubjectTeacherCrossRef(subjectId, teacherId))
        }
    }
}

private fun TeacherEntity.toDomain() = Teacher(id = id, firstName = firstName, lastName = lastName)

private fun Teacher.toEntity() = TeacherEntity(
    id = id.ifEmpty { UUID.randomUUID().toString() },
    firstName = firstName,
    lastName = lastName
)

private fun ClassPresetEntity.toDomain() = ClassPreset(
    id = id,
    name = name,
    room = room,
    teacher = teacher,
    hexColor = hexColor
)
