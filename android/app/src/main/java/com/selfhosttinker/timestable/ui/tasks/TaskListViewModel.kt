package com.selfhosttinker.timestable.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.datastore.AppSettings
import com.selfhosttinker.timestable.data.datastore.SettingsDataStore
import com.selfhosttinker.timestable.data.repository.TaskRepository
import com.selfhosttinker.timestable.domain.model.StudyTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val allTasks: StateFlow<List<StudyTask>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun completeTask(task: StudyTask, grade: Double?) {
        viewModelScope.launch {
            taskRepository.saveTask(task.copy(isCompleted = true, grade = grade))
        }
    }

    fun uncompleteTask(task: StudyTask) {
        viewModelScope.launch {
            taskRepository.saveTask(task.copy(isCompleted = false))
        }
    }

    fun deleteTask(task: StudyTask) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
}
