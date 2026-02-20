package com.selfhosttinker.timestable.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val showWeekends: Boolean = true,
    val numberOfWeeks: Int = 1,
    val repeatingWeeksEnabled: Boolean = false,
    val averageType: String = "arithmetic",
    val gradeRangeMax: Int = 10,
    val notificationsEnabled: Boolean = false
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SHOW_WEEKENDS = booleanPreferencesKey("show_weekends")
        val NUMBER_OF_WEEKS = intPreferencesKey("number_of_weeks")
        val REPEATING_WEEKS_ENABLED = booleanPreferencesKey("repeating_weeks_enabled")
        val AVERAGE_TYPE = stringPreferencesKey("average_type")
        val GRADE_RANGE_MAX = intPreferencesKey("grade_range_max")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            AppSettings(
                showWeekends = prefs[Keys.SHOW_WEEKENDS] ?: true,
                numberOfWeeks = prefs[Keys.NUMBER_OF_WEEKS] ?: 1,
                repeatingWeeksEnabled = prefs[Keys.REPEATING_WEEKS_ENABLED] ?: false,
                averageType = prefs[Keys.AVERAGE_TYPE] ?: "arithmetic",
                gradeRangeMax = prefs[Keys.GRADE_RANGE_MAX] ?: 10,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: false
            )
        }

    suspend fun setShowWeekends(value: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_WEEKENDS] = value }
    }

    suspend fun setNumberOfWeeks(value: Int) {
        context.dataStore.edit { it[Keys.NUMBER_OF_WEEKS] = value }
    }

    suspend fun setRepeatingWeeksEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.REPEATING_WEEKS_ENABLED] = value }
    }

    suspend fun setAverageType(value: String) {
        context.dataStore.edit { it[Keys.AVERAGE_TYPE] = value }
    }

    suspend fun setGradeRangeMax(value: Int) {
        context.dataStore.edit { it[Keys.GRADE_RANGE_MAX] = value }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = value }
    }
}
