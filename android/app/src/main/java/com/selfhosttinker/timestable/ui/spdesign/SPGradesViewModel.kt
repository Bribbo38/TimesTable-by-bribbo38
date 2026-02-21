package com.selfhosttinker.timestable.ui.spdesign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.datastore.AppSettings
import com.selfhosttinker.timestable.data.datastore.SettingsDataStore
import com.selfhosttinker.timestable.data.repository.GradeEntryRepository
import com.selfhosttinker.timestable.data.repository.PresetRepository
import com.selfhosttinker.timestable.domain.GradeScale
import com.selfhosttinker.timestable.domain.model.ClassPreset
import com.selfhosttinker.timestable.domain.model.GradeEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SubjectGradeGroup(
    val subjectName: String,
    val hexColor: String,
    val entries: List<GradeEntry>,
    val weightedAverage: Double,
    val count: Int
)

@HiltViewModel
class SPGradesViewModel @Inject constructor(
    private val gradeEntryRepository: GradeEntryRepository,
    private val presetRepository: PresetRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val presets: StateFlow<List<ClassPreset>> = presetRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjectGroups: StateFlow<List<SubjectGradeGroup>> =
        gradeEntryRepository.getAllEntries()
            .map { entries ->
                entries
                    .groupBy { it.subjectName }
                    .map { (subject, subjectEntries) ->
                        val weightedAvg = computeWeightedAverage(subjectEntries)
                        SubjectGradeGroup(
                            subjectName = subject,
                            hexColor = subjectEntries.firstOrNull()?.hexColor ?: "#1565C0",
                            entries = subjectEntries,
                            weightedAverage = weightedAvg,
                            count = subjectEntries.size
                        )
                    }
                    .filter { it.count > 0 }
                    .sortedBy { it.subjectName }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallAverage: StateFlow<Double> = subjectGroups
        .map { groups ->
            val validGroups = groups.filter { !it.weightedAverage.isNaN() }
            if (validGroups.isEmpty()) Double.NaN
            else validGroups.map { it.weightedAverage }.average()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Double.NaN)

    fun saveEntry(entry: GradeEntry) {
        viewModelScope.launch {
            gradeEntryRepository.saveEntry(
                entry.copy(id = entry.id.ifEmpty { UUID.randomUUID().toString() })
            )
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            gradeEntryRepository.deleteEntry(id)
        }
    }

    private fun computeWeightedAverage(entries: List<GradeEntry>): Double {
        val totalWeight = entries.sumOf { it.weight }
        return if (totalWeight == 0.0) Double.NaN
        else entries.sumOf { it.value * it.weight } / totalWeight
    }
}
