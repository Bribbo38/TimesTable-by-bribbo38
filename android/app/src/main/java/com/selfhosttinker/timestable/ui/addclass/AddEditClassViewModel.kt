package com.selfhosttinker.timestable.ui.addclass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.datastore.SettingsDataStore
import com.selfhosttinker.timestable.data.repository.ClassRepository
import com.selfhosttinker.timestable.data.repository.PresetRepository
import com.selfhosttinker.timestable.domain.model.ClassPreset
import com.selfhosttinker.timestable.domain.model.SchoolClass
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditClassState(
    val id: String = "",
    val name: String = "",
    val room: String = "",
    val teacher: String = "",
    val notes: String = "",
    val dayOfWeek: Int = 1,
    val weekIndex: Int = 1,
    val startTimeMs: Long = 8 * 60 * 60_000L,   // 08:00 as ms-since-midnight
    val endTimeMs: Long = 9 * 60 * 60_000L,     // 09:00
    val hexColor: String = "#0A84FF",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditClassViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val presetRepository: PresetRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditClassState())
    val state: StateFlow<AddEditClassState> = _state.asStateFlow()

    private val _overlappingClasses = MutableStateFlow<List<SchoolClass>>(emptyList())
    val overlappingClasses: StateFlow<List<SchoolClass>> = _overlappingClasses.asStateFlow()

    val presets: StateFlow<List<ClassPreset>> = presetRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val defaultDurationMs: StateFlow<Long> = settingsDataStore.settingsFlow
        .map { it.defaultClassDurationMin * 60_000L }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 3_600_000L)

    init {
        viewModelScope.launch {
            val durationMs = settingsDataStore.settingsFlow.first().defaultClassDurationMin * 60_000L
            if (_state.value.id.isEmpty()) {
                _state.update { it.copy(endTimeMs = it.startTimeMs + durationMs) }
            }
        }
    }

    fun loadClass(classId: String?) {
        if (classId == null) return
        viewModelScope.launch {
            val schoolClass = classRepository.getClassById(classId) ?: return@launch
            _state.update {
                it.copy(
                    id = schoolClass.id,
                    name = schoolClass.name,
                    room = schoolClass.room ?: "",
                    teacher = schoolClass.teacher ?: "",
                    notes = schoolClass.notes ?: "",
                    dayOfWeek = schoolClass.dayOfWeek,
                    weekIndex = schoolClass.weekIndex,
                    startTimeMs = schoolClass.startTimeMs,
                    endTimeMs = schoolClass.endTimeMs,
                    hexColor = schoolClass.hexColor
                )
            }
        }
    }

    fun initDay(day: Int) {
        if (day > 0 && _state.value.id.isEmpty()) {
            _state.update { it.copy(dayOfWeek = day) }
        }
    }

    fun applyPreset(preset: ClassPreset) {
        _state.update {
            it.copy(
                name = preset.name,
                room = preset.room ?: it.room,
                teacher = preset.teacher ?: it.teacher,
                hexColor = preset.hexColor
            )
        }
    }

    fun updateName(value: String)      { _state.update { it.copy(name = value) } }
    fun updateRoom(value: String)      { _state.update { it.copy(room = value) } }
    fun updateTeacher(value: String)   { _state.update { it.copy(teacher = value) } }
    fun updateNotes(value: String)     { _state.update { it.copy(notes = value) } }
    fun updateDayOfWeek(value: Int)    { _state.update { it.copy(dayOfWeek = value) } }
    fun updateWeekIndex(value: Int)    { _state.update { it.copy(weekIndex = value) } }
    fun updateEndTime(ms: Long)        { _state.update { it.copy(endTimeMs = ms) } }
    fun updateColor(hex: String)       { _state.update { it.copy(hexColor = hex) } }

    fun updateStartTime(ms: Long) {
        _state.update { it.copy(startTimeMs = ms, endTimeMs = ms + defaultDurationMs.value) }
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) return
        if (s.startTimeMs >= s.endTimeMs) return
        viewModelScope.launch {
            val conflicts = classRepository.getOverlappingClasses(
                s.dayOfWeek, s.startTimeMs, s.endTimeMs, s.id
            )
            if (conflicts.isNotEmpty()) {
                _overlappingClasses.value = conflicts
            } else {
                doSave()
            }
        }
    }

    fun saveForce() { viewModelScope.launch { doSave() } }

    fun clearOverlapWarning() { _overlappingClasses.value = emptyList() }

    private suspend fun doSave() {
        val s = _state.value
        if (s.startTimeMs >= s.endTimeMs) return
        val schoolClass = SchoolClass(
            id = s.id.ifEmpty { UUID.randomUUID().toString() },
            name = s.name.trim(),
            room = s.room.trim().ifEmpty { null },
            teacher = s.teacher.trim().ifEmpty { null },
            notes = s.notes.trim().ifEmpty { null },
            dayOfWeek = s.dayOfWeek,
            weekIndex = s.weekIndex,
            startTimeMs = s.startTimeMs,
            endTimeMs = s.endTimeMs,
            hexColor = s.hexColor
        )
        classRepository.saveClass(schoolClass)
        _state.update { it.copy(isSaved = true) }
    }
}
