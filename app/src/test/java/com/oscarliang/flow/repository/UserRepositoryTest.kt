package com.oscarliang.flow.repository

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.lifecycle.Observer
import com.oscarliang.flow.repository.UserRepository.Companion.DARK_MODE_KEY
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UserRepositoryTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val preferences = mockk<SharedPreferences>(relaxed = true)
    private lateinit var repository: UserRepository

    @Before
    fun init() {
        every { preferences.getString(any(), any()) } returns DarkMode.DEFAULT.name
        repository = UserRepository(preferences)
    }

    @Test
    fun testUpdateDarkMode() {
        val darkMode = DarkMode.DARK
        val observer = mockk<Observer<DarkMode>>(relaxed = true)
        repository.darkModeLiveData.observeForever(observer)
        repository.updateDarkMode(darkMode)
        verify { preferences.edit { putString(DARK_MODE_KEY, darkMode.name) } }
        verify { observer.onChanged(darkMode) }
    }

}