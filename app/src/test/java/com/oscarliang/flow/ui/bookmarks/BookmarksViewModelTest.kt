package com.oscarliang.flow.ui.bookmarks

import com.oscarliang.flow.repository.NewsRepository
import com.oscarliang.flow.util.MainDispatcherRule
import com.oscarliang.flow.util.TestUtil
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BookmarksViewModelTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<NewsRepository>(relaxed = true)
    private lateinit var viewModel: BookmarksViewModel

    @Before
    fun init() {
        viewModel = BookmarksViewModel(repository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun update() = runTest {
        val current = TestUtil.createNews("foo", "bar", "src")
        val updated = current.copy(bookmark = true)
        viewModel.toggleBookmark(current)
        advanceUntilIdle()
        coVerify { repository.updateNews(updated) }
    }

}