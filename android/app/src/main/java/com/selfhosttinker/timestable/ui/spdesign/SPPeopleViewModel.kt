package com.selfhosttinker.timestable.ui.spdesign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.repository.ClassRepository
import com.selfhosttinker.timestable.data.repository.PresetRepository
import com.selfhosttinker.timestable.data.repository.TeacherRepository
import com.selfhosttinker.timestable.domain.model.ClassPreset
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.domain.model.Teacher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ClassPresetWithTeachers(val preset: ClassPreset, val teachers: List<Teacher>)
data class TeacherWithSubjects(val teacher: Teacher, val subjects: List<ClassPreset>)

@HiltViewModel
class SPPeopleViewModel @Inject constructor(
    private val presetRepo: PresetRepository,
    private val teacherRepo: TeacherRepository,
    private val classRepo: ClassRepository
) : ViewModel() {

    val allTeachers: StateFlow<List<Teacher>> = teacherRepo.getAllTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes: StateFlow<List<SchoolClass>> = classRepo.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val subjects: StateFlow<List<ClassPresetWithTeachers>> = presetRepo.getAllPresets()
        .flatMapLatest { presets ->
            if (presets.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(presets.map { preset ->
                    teacherRepo.getTeachersForSubject(preset.id).map { teachers ->
                        ClassPresetWithTeachers(preset, teachers)
                    }
                }) { array -> array.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val teachers: StateFlow<List<TeacherWithSubjects>> = teacherRepo.getAllTeachers()
        .flatMapLatest { teachers ->
            if (teachers.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(teachers.map { teacher ->
                    teacherRepo.getSubjectsForTeacher(teacher.id).map { subjects ->
                        TeacherWithSubjects(teacher, subjects)
                    }
                }) { array -> array.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSubject(preset: ClassPreset, teacherIds: List<String>) {
        viewModelScope.launch {
            val id = preset.id.ifEmpty { UUID.randomUUID().toString() }
            presetRepo.savePreset(preset.copy(id = id))
            teacherRepo.setTeachersForSubject(id, teacherIds)
        }
    }

    fun deleteSubject(preset: ClassPreset) {
        viewModelScope.launch {
            presetRepo.deletePreset(preset)
        }
    }

    fun saveTeacher(firstName: String, lastName: String, id: String = "") {
        viewModelScope.launch {
            teacherRepo.saveTeacher(
                Teacher(
                    id = id.ifEmpty { UUID.randomUUID().toString() },
                    firstName = firstName,
                    lastName = lastName
                )
            )
        }
    }

    fun deleteTeacher(id: String) {
        viewModelScope.launch {
            teacherRepo.deleteTeacher(id)
        }
    }
}
