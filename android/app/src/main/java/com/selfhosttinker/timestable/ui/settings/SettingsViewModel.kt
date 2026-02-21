package com.selfhosttinker.timestable.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.datastore.AppSettings
import com.selfhosttinker.timestable.data.datastore.SettingsDataStore
import com.selfhosttinker.timestable.data.export.ExportManager
import com.selfhosttinker.timestable.data.repository.ClassRepository
import com.selfhosttinker.timestable.data.repository.PresetRepository
import com.selfhosttinker.timestable.data.repository.TaskRepository
import com.selfhosttinker.timestable.domain.AverageType
import com.selfhosttinker.timestable.domain.model.ClassPreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val classRepository: ClassRepository,
    private val taskRepository: TaskRepository,
    private val presetRepository: PresetRepository,
    private val exportManager: ExportManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val presets: StateFlow<List<ClassPreset>> = presetRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setShowWeekends(value: Boolean) = viewModelScope.launch { settingsDataStore.setShowWeekends(value) }
    fun setRepeatingWeeks(value: Boolean) = viewModelScope.launch { settingsDataStore.setRepeatingWeeksEnabled(value) }
    fun setNumberOfWeeks(value: Int) = viewModelScope.launch { settingsDataStore.setNumberOfWeeks(value) }
    fun setAverageType(value: String) = viewModelScope.launch { settingsDataStore.setAverageType(value) }
    fun setGradeScale(value: String) = viewModelScope.launch { settingsDataStore.setGradeScale(value) }
    fun setNotificationsEnabled(value: Boolean) = viewModelScope.launch { settingsDataStore.setNotificationsEnabled(value) }
    fun setDefaultClassDurationMin(value: Int) = viewModelScope.launch { settingsDataStore.setDefaultClassDurationMin(value) }
    fun setUseHamburgerNav(value: Boolean) = viewModelScope.launch { settingsDataStore.setUseHamburgerNav(value) }
    fun setUseSpTheme(value: Boolean)      = viewModelScope.launch { settingsDataStore.setUseSchoolPlannerTheme(value) }

    fun addPreset(name: String, room: String?, teacher: String?, hexColor: String) {
        viewModelScope.launch {
            presetRepository.savePreset(
                ClassPreset(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    room = room,
                    teacher = teacher,
                    hexColor = hexColor
                )
            )
        }
    }

    fun deletePreset(preset: ClassPreset) {
        viewModelScope.launch { presetRepository.deletePreset(preset) }
    }

    fun resetAll() {
        viewModelScope.launch {
            classRepository.deleteAllClasses()
            taskRepository.deleteAllTasks()
            presetRepository.deleteAllPresets()
        }
    }

    fun exportJson(context: Context) {
        viewModelScope.launch {
            try {
                val file = exportManager.exportToFile(context)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export Timetable").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importJson(context: Context, jsonString: String) {
        viewModelScope.launch {
            try {
                exportManager.importFromJson(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
