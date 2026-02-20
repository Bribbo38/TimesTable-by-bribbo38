package com.selfhosttinker.timestable.ui.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.datastore.AppSettings
import com.selfhosttinker.timestable.data.datastore.SettingsDataStore
import com.selfhosttinker.timestable.data.repository.TaskRepository
import com.selfhosttinker.timestable.domain.AverageType
import com.selfhosttinker.timestable.domain.model.StudyTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SubjectGrades(
    val subjectName: String,
    val hexColor: String,
    val grades: List<Double>,
    val average: Double
)

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val subjectGrades: StateFlow<List<SubjectGrades>> = combine(
        taskRepository.getTasksWithGrades(),
        settingsDataStore.settingsFlow
    ) { tasks, settings ->
        val avgType = AverageType.fromRawValue(settings.averageType)
        tasks
            .groupBy { it.subjectName }
            .map { (subject, subjectTasks) ->
                val grades = subjectTasks.mapNotNull { it.grade }
                SubjectGrades(
                    subjectName = subject,
                    hexColor = subjectTasks.firstOrNull()?.hexColor ?: "#0A84FF",
                    grades = grades,
                    average = avgType.compute(grades)
                )
            }
            .filter { it.grades.isNotEmpty() }
            .sortedBy { it.subjectName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallAverage: StateFlow<Double> = combine(
        subjectGrades,
        settingsDataStore.settingsFlow
    ) { subjects, settings ->
        val allGrades = subjects.flatMap { it.grades }
        if (allGrades.isEmpty()) 0.0
        else AverageType.fromRawValue(settings.averageType).compute(allGrades)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
