package com.selfhosttinker.timestable.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfhosttinker.timestable.data.datastore.AppSettings
import com.selfhosttinker.timestable.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setNavStyle(useHamburger: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setUseHamburgerNav(useHamburger)
            settingsDataStore.setNavStyleChosen(true)
        }
    }
}
