package com.selfhosttinker.timestable.data.db.dao

import androidx.room.*
import com.selfhosttinker.timestable.data.db.entity.ClassPresetEntity
import com.selfhosttinker.timestable.data.db.entity.SubjectTeacherCrossRef
import com.selfhosttinker.timestable.data.db.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectTeacherDao {

    @Query("""
        SELECT t.* FROM teachers t
        INNER JOIN subject_teacher_cross_ref x ON t.id = x.teacherId
        WHERE x.subjectId = :subjectId
        ORDER BY t.lastName ASC
    """)
    fun getTeachersForSubject(subjectId: String): Flow<List<TeacherEntity>>

    @Query("""
        SELECT p.* FROM class_presets p
        INNER JOIN subject_teacher_cross_ref x ON p.id = x.subjectId
        WHERE x.teacherId = :teacherId
    """)
    fun getSubjectsForTeacher(teacherId: String): Flow<List<ClassPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ref: SubjectTeacherCrossRef)

    @Query("DELETE FROM subject_teacher_cross_ref WHERE subjectId = :subjectId")
    suspend fun clearTeachersForSubject(subjectId: String)

    @Query("DELETE FROM subject_teacher_cross_ref WHERE teacherId = :teacherId")
    suspend fun clearSubjectsForTeacher(teacherId: String)
}
