package com.oscarliang.flow.ui.newsdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.flow.model.News
import com.oscarliang.flow.repository.NewsRepository
import com.oscarliang.flow.util.MainDispatcherRule
import com.oscarliang.flow.util.TestUtil
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NewsDetailViewModelTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutor = InstantTaskExecutorRule()

    private val repository = mockk<NewsRepository>(relaxed = true)
    private lateinit var viewModel: NewsDetailViewModel

    @Before
    fun init() {
        viewModel = NewsDetailViewModel(repository)
    }

    @Test
    fun testSearchWithoutObserver() {
        viewModel.setNewsId("foo")
        verify { repository wasNot Called }
    }

    @Test
    fun testSearchWhenObserved() {
        val dbData = MutableLiveData<News>()
        every { repository.getNewsById(any()) } returns dbData
        viewModel.news.observeForever(mockk(relaxed = true))
        viewModel.moreNews.observeForever(mockk(relaxed = true))

        viewModel.setNewsId("foo")
        val fooNews = TestUtil.createNews("foo", "foo_body", "foo_src")
        dbData.postValue(fooNews)
        verify { repository.getNewsById("foo") }
        verify { repository.getNewsBySource("foo_src", any()) }

        viewModel.setNewsId("bar")
        val barNews = TestUtil.createNews("bar", "bar_body", "bar_src")
        dbData.postValue(barNews)
        verify { repository.getNewsById("bar") }
        verify { repository.getNewsBySource("bar_src", any()) }
    }

    @Test
    fun testChangeWhileObserved() {
        val dbData = MutableLiveData<News>()
        every { repository.getNewsById(any()) } returns dbData
        viewModel.news.observeForever(mockk(relaxed = true))
        viewModel.moreNews.observeForever(mockk(relaxed = true))

        viewModel.setNewsId("foo")
        val fooNews = TestUtil.createNews("foo", "foo_body", "foo_src")
        dbData.postValue(fooNews)
        viewModel.setNewsId("bar")
        val barNews = TestUtil.createNews("bar", "bar_body", "bar_src")
        dbData.postValue(barNews)

        verify { repository.getNewsById("foo") }
        verify { repository.getNewsBySource("foo_src", any()) }
        verify { repository.getNewsById("bar") }
        verify { repository.getNewsBySource("bar_src", any()) }
    }

    @Test
    fun testRetry() {
        viewModel.retry()
        verify { repository wasNot Called }

        viewModel.setNewsId("foo")
        viewModel.retry()
        verify { repository wasNot Called }

        viewModel.news.observeForever(mockk())
        verify { repository.getNewsById("foo") }
        clearMocks(repository)

        viewModel.retry()
        verify { repository.getNewsById("foo") }
    }

    @Test
    fun testBlankQuery() {
        viewModel.news.observeForever(mockk(relaxed = true))
        viewModel.setNewsId("foo")
        verify { repository.getNewsById("foo") }
        clearMocks(repository)

        viewModel.setNewsId("")
        verify { repository wasNot Called }
    }

    @Test
    fun testBlankSource() {
        val dbData = MutableLiveData<News>()
        every { repository.getNewsById("foo") } returns dbData
        viewModel.news.observeForever(mockk(relaxed = true))
        viewModel.moreNews.observeForever(mockk(relaxed = true))

        viewModel.setNewsId("foo")
        val news = TestUtil.createNews("foo", "bar", "")
        dbData.postValue(news)
        verify { repository.getNewsById("foo") }
        verify { repository.getNewsBySource(any(), any()) wasNot Called }
    }

    @Test
    fun testResetQuery() {
        val observer = mockk<Observer<String?>>(relaxed = true)
        viewModel.id.observeForever(observer)

        viewModel.setNewsId("foo")
        verify { observer.onChanged("foo") }
        clearMocks(observer)

        viewModel.setNewsId("foo")
        verify { observer wasNot Called }
        viewModel.setNewsId("bar")
        verify { observer.onChanged("bar") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testUpdate() = runTest {
        val current = TestUtil.createNews("foo", "bar", "src")
        val updated = current.copy(bookmark = true)
        viewModel.toggleBookmark(current)
        advanceUntilIdle()
        coVerify { repository.updateNews(updated) }
    }

}