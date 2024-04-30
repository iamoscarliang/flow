package com.oscarliang.flow.ui.news

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.flow.model.Category
import com.oscarliang.flow.repository.CategoryRepository
import com.oscarliang.flow.repository.NewsRepository
import com.oscarliang.flow.util.MainDispatcherRule
import com.oscarliang.flow.util.Resource
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NewsViewModelTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutor = InstantTaskExecutorRule()

    private val newsRepository = mockk<NewsRepository>(relaxed = true)
    private val categoryRepository = mockk<CategoryRepository>(relaxed = true)
    private lateinit var viewModel: NewsViewModel

    @Before
    fun init() {
        viewModel = NewsViewModel(newsRepository, categoryRepository)
    }

    @Test
    fun testSearchWithoutObserver() {
        viewModel.setLatestQuery("foo", 10)
        viewModel.setCategoryQuery("foo", 10)
        verify { newsRepository wasNot Called }
    }

    @Test
    fun testSearchWhenObserved() {
        val dbData = MutableLiveData<Category>()
        every { categoryRepository.getSelectedCategory() } returns dbData
        viewModel.latestNews.observeForever(mockk())
        viewModel.categoryNews.observeForever(mockk())

        viewModel.setLatestQuery("foo", 10)
        viewModel.setCategoryQuery("foo", 10)
        dbData.postValue(Category("cat", "cat"))
        verify { newsRepository.getLatestNews("foo", 10) }
        verify { newsRepository.getNewsByCategory("cat", "foo", 10) }
        clearMocks(newsRepository)

        viewModel.setLatestQuery("bar", 10)
        viewModel.setCategoryQuery("bar", 10)
        verify { newsRepository.getLatestNews("bar", 10) }
        verify { newsRepository.getNewsByCategory("cat", "bar", 10) }
    }

    @Test
    fun testChangeWhileObserved() {
        val dbData = MutableLiveData<Category>()
        every { categoryRepository.getSelectedCategory() } returns dbData
        viewModel.latestNews.observeForever(mockk())
        viewModel.categoryNews.observeForever(mockk())

        viewModel.setLatestQuery("foo", 10)
        viewModel.setLatestQuery("bar", 10)
        verify { newsRepository.getLatestNews("foo", 10) }
        verify { newsRepository.getLatestNews("bar", 10) }

        dbData.postValue(Category("cat", "cat"))
        viewModel.setCategoryQuery("foo", 10)
        viewModel.setCategoryQuery("bar", 10)
        verify { newsRepository.getNewsByCategory("cat", "foo", 10) }
        verify { newsRepository.getNewsByCategory("cat", "bar", 10) }
    }

    @Test
    fun testSearchNextPageWithoutQuery() {
        viewModel.categoryNews.observeForever(mockk())
        viewModel.loadNextPage()
        verify { newsRepository wasNot Called }
    }

    @Test
    fun testSearchNextPageWithQuery() {
        val dbData = MutableLiveData<Category>()
        every { categoryRepository.getSelectedCategory() } returns dbData
        viewModel.categoryNews.observeForever(mockk())

        viewModel.setCategoryQuery("foo", 10)
        dbData.postValue(Category("cat", "cat"))
        verify { newsRepository.getNewsByCategory("cat", "foo", 10) }
        clearMocks(newsRepository)

        viewModel.loadNextPage()
        verify { newsRepository.getCategoryNextPage("cat", "foo", 10) }
    }

    @Test
    fun testSearchNextPageWhenChangeQuery() {
        val dbData = MutableLiveData<Category>()
        every { categoryRepository.getSelectedCategory() } returns dbData
        val nextPage = MutableLiveData<Resource<Boolean>?>()
        every { newsRepository.getCategoryNextPage(any(), any(), 10) } returns nextPage
        viewModel.categoryNews.observeForever(mockk(relaxed = true))

        viewModel.setCategoryQuery("foo", 10)
        dbData.postValue(Category("cat1", "cat1"))
        verify { newsRepository.getNewsByCategory("cat1", "foo", 10) }

        viewModel.loadMoreState.observeForever(mockk(relaxed = true))
        viewModel.loadNextPage()
        verify { newsRepository.getCategoryNextPage("cat1", "foo", 10) }
        assertEquals(nextPage.hasActiveObservers(), true)

        viewModel.setCategoryQuery("bar", 10)
        dbData.postValue(Category("cat2", "cat2"))
        assertEquals(nextPage.hasActiveObservers(), false)
        verify { newsRepository.getNewsByCategory("cat2", "bar", 10) }
        verify { newsRepository.getCategoryNextPage("cat2", "bar", 10) wasNot Called }
    }

    @Test
    fun testRefresh() {
        val dbData = MutableLiveData<Category>()
        every { categoryRepository.getSelectedCategory() } returns dbData

        viewModel.refresh()
        verify { newsRepository wasNot Called }

        viewModel.setLatestQuery("foo", 10)
        viewModel.setCategoryQuery("bar", 10)
        dbData.postValue(Category("cat", "cat"))
        viewModel.refresh()
        verify { newsRepository wasNot Called }

        viewModel.latestNews.observeForever(mockk())
        viewModel.categoryNews.observeForever(mockk())
        verify { newsRepository.getLatestNews("foo", 10) }
        verify { newsRepository.getNewsByCategory("cat", "bar", 10) }
        clearMocks(newsRepository)

        viewModel.refresh()
        verify { newsRepository.getLatestNews("foo", 10) }
        verify { newsRepository.getNewsByCategory("cat", "bar", 10) }
    }

    @Test
    fun testBlankQuery() {
        val dbData = MutableLiveData<Category>()
        every { categoryRepository.getSelectedCategory() } returns dbData

        viewModel.latestNews.observeForever(mockk(relaxed = true))
        viewModel.categoryNews.observeForever(mockk(relaxed = true))
        viewModel.setLatestQuery("foo", 10)
        viewModel.setCategoryQuery("bar", 10)
        dbData.postValue(Category("cat", "cat"))
        verify { newsRepository.getLatestNews("foo", 10) }
        verify { newsRepository.getNewsByCategory("cat", "bar", 10) }
        clearMocks(newsRepository)

        viewModel.setLatestQuery("", 10)
        viewModel.setCategoryQuery("", 10)
        verify { newsRepository wasNot Called }
    }

    @Test
    fun testResetQuery() {
        val dbData = MutableLiveData<Category>()
        every { categoryRepository.getSelectedCategory() } returns dbData

        val latestObserver = mockk<Observer<NewsViewModel.Query>>(relaxed = true)
        val categoryObserver = mockk<Observer<NewsViewModel.CategoryState>>(relaxed = true)
        viewModel.query.observeForever(latestObserver)
        viewModel.categoryState.observeForever(categoryObserver)

        viewModel.setLatestQuery("foo", 10)
        viewModel.setCategoryQuery("foo", 10)
        dbData.postValue(Category("cat", "cat"))
        verify { latestObserver.onChanged(NewsViewModel.Query("foo", 10)) }
        verify {
            categoryObserver.onChanged(
                NewsViewModel.CategoryState(
                    "cat",
                    NewsViewModel.Query("foo", 10)
                )
            )
        }
        clearMocks(latestObserver)

        viewModel.setLatestQuery("foo", 10)
        viewModel.setCategoryQuery("foo", 10)
        verify { latestObserver wasNot Called }
        viewModel.setLatestQuery("bar", 10)
        viewModel.setCategoryQuery("bar", 10)
        verify { latestObserver.onChanged(NewsViewModel.Query("bar", 10)) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSelect() = runTest {
        val updated = TestUtil.createCategory("cat", "cat")
        viewModel.selectCategory(updated)
        advanceUntilIdle()
        coVerify { categoryRepository.updateCategory(updated) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testUpdate() = runTest {
        val current = TestUtil.createNews("foo", "bar", "src")
        val updated = current.copy(bookmark = true)
        viewModel.toggleBookmark(current)
        advanceUntilIdle()
        coVerify { newsRepository.updateNews(updated) }
    }

}