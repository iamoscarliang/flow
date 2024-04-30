package com.oscarliang.flow.ui.settings

import com.oscarliang.flow.repository.DarkMode
import com.oscarliang.flow.repository.UserRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SettingsViewModelTest {

    private val repository = mockk<UserRepository>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun init() {
        viewModel = SettingsViewModel(repository)
    }

    @Test
    fun testUpdate() {
        val updated = DarkMode.DARK
        viewModel.updateDarkMode(updated)
        verify { repository.updateDarkMode(updated) }
    }

}