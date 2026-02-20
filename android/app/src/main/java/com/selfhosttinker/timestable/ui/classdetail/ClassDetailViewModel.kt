package com.selfhosttinker.timestable.ui.classdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.repository.ClassRepository
import com.selfhosttinker.timestable.data.repository.TaskRepository
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.domain.model.StudyTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _classId = MutableStateFlow("")
    val schoolClass: StateFlow<SchoolClass?> = _classId
        .filter { it.isNotEmpty() }
        .flatMapLatest { id ->
            flow { emit(classRepository.getClassById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tasks: StateFlow<List<StudyTask>> = _classId
        .filter { it.isNotEmpty() }
        .flatMapLatest { id -> taskRepository.getTasksByClass(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    fun loadClass(classId: String) {
        _classId.value = classId
    }

    fun deleteClass() {
        viewModelScope.launch {
            val cls = schoolClass.value ?: return@launch
            classRepository.deleteClass(cls)
            _isDeleted.value = true
        }
    }

    fun addTask(title: String, detail: String?, dueDateMs: Long) {
        val cls = schoolClass.value ?: return
        viewModelScope.launch {
            taskRepository.saveTask(
                StudyTask(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    detail = detail,
                    dueDateMs = dueDateMs,
                    isCompleted = false,
                    hexColor = cls.hexColor,
                    subjectName = cls.name,
                    linkedClassId = cls.id
                )
            )
        }
    }

    fun toggleTask(task: StudyTask) {
        viewModelScope.launch {
            taskRepository.saveTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: StudyTask) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
}
