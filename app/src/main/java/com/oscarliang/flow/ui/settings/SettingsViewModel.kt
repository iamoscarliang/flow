package com.oscarliang.flow.ui.settings

import androidx.lifecycle.ViewModel
import com.oscarliang.flow.repository.DarkMode
import com.oscarliang.flow.repository.UserRepository

class SettingsViewModel(
    private val repository: UserRepository
) : ViewModel() {

    val darkModeLiveData = repository.darkModeLiveData

    fun updateDarkMode(darkMode: DarkMode) {
        repository.updateDarkMode(darkMode)
    }

}