package com.oscarliang.flow.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class UserRepository(
    private val sharedPreferences: SharedPreferences
) {

    private val _darkModeLiveData = MutableLiveData(darkMode)
    val darkModeLiveData: LiveData<DarkMode>
        get() = _darkModeLiveData

    private val darkMode: DarkMode
        get() {
            val mode = sharedPreferences.getString(DARK_MODE_KEY, DarkMode.DEFAULT.name)
            return DarkMode.valueOf(mode ?: DarkMode.DEFAULT.name)
        }

    fun updateDarkMode(darkMode: DarkMode) {
        sharedPreferences.edit {
            putString(DARK_MODE_KEY, darkMode.name)
        }
        _darkModeLiveData.value = darkMode
    }

    companion object {
        const val DARK_MODE_KEY = "dark_mode"
    }

}

enum class DarkMode {
    DEFAULT,
    LIGHT,
    DARK
}