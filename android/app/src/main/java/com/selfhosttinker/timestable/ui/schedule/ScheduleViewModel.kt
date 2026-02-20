package com.selfhosttinker.timestable.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.datastore.AppSettings
import com.selfhosttinker.timestable.data.datastore.SettingsDataStore
import com.selfhosttinker.timestable.data.repository.ClassRepository
import com.selfhosttinker.timestable.domain.model.SchoolClass
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _selectedDay = MutableStateFlow(todayAppDay())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _selectedWeek = MutableStateFlow(1)
    val selectedWeek: StateFlow<Int> = _selectedWeek.asStateFlow()

    val classesForDay: StateFlow<List<SchoolClass>> = combine(
        _selectedDay,
        _selectedWeek,
        settingsDataStore.settingsFlow
    ) { day, week, settings ->
        Triple(day, week, settings)
    }.flatMapLatest { (day, week, _) ->
        classRepository.getClassesByDayAndWeek(day, week)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(day: Int) { _selectedDay.value = day }

    fun selectWeek(week: Int) { _selectedWeek.value = week }

    companion object {
        fun todayAppDay(): Int {
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY    -> 7
                Calendar.MONDAY    -> 1
                Calendar.TUESDAY   -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY  -> 4
                Calendar.FRIDAY    -> 5
                Calendar.SATURDAY  -> 6
                else -> 1
            }
        }
    }
}
