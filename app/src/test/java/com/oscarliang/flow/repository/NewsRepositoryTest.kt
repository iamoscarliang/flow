package com.oscarliang.flow.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.room.withTransaction
import com.oscarliang.flow.api.NewsSearchResponse
import com.oscarliang.flow.api.NewsService
import com.oscarliang.flow.db.NewsDao
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.model.News
import com.oscarliang.flow.model.NewsSearchResult
import com.oscarliang.flow.repository.NewsRepository.Companion.LATEST_NEWS_KEY
import com.oscarliang.flow.util.MainDispatcherRule
import com.oscarliang.flow.util.RateLimiter
import com.oscarliang.flow.util.Resource
import com.oscarliang.flow.util.TestUtil
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class NewsRepositoryTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dao = mockk<NewsDao>(relaxed = true)
    private val service = mockk<NewsService>(relaxed = true)
    private val rateLimiter = mockk<RateLimiter<String>>(relaxed = true)
    private lateinit var repository: NewsRepository

    @Before
    fun init() {
        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )
        val db = mockk<NewsDatabase>(relaxed = true)
        val transaction = slot<suspend () -> Unit>()
        coEvery { db.withTransaction(capture(transaction)) } coAnswers {
            transaction.captured.invoke()
        }
        every { db.newsDao() } returns dao
        repository = NewsRepository(
            db = db,
            newsDao = dao,
            service = service,
            rateLimiter = rateLimiter
        )
    }

    @Test
    fun testGetLatestNewsFromDb() = runTest {
        every { rateLimiter.shouldFetch(LATEST_NEWS_KEY) } returns false
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult(LATEST_NEWS_KEY) } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        coEvery { dao.findNewsSearchResult(LATEST_NEWS_KEY) } returns null

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getLatestNews("2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult(LATEST_NEWS_KEY, 2, ids)
        dbSearchResult.postValue(searchResult)
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        dbData.postValue(news)
        verify { observer.onChanged(Resource.success(news)) }
        verify { service wasNot Called }
    }

    @Test
    fun testGetLatestNewsFromNetwork() = runTest {
        every { rateLimiter.shouldFetch(LATEST_NEWS_KEY) } returns true
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult(LATEST_NEWS_KEY) } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        coEvery { dao.findNewsSearchResult(LATEST_NEWS_KEY) } returns null
        val response = NewsSearchResponse(NewsSearchResponse.Article(2, news))
        coEvery { service.getLatestNews(date = "2024-04-29", count = 10) } returns response

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getLatestNews("2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult(LATEST_NEWS_KEY, 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(news)
        coVerify { service.getLatestNews(date = "2024-04-29", count = 10) }
        coVerify { dao.insertNews(news) }
        coVerify { dao.insertNewsSearchResult(searchResult) }
        verify { observer.onChanged(Resource.success(news)) }
    }

    @Test
    fun testGetLatestNewsFromNetworkError() = runTest {
        every { rateLimiter.shouldFetch(LATEST_NEWS_KEY) } returns true
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult(LATEST_NEWS_KEY) } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        coEvery { dao.findNewsSearchResult(LATEST_NEWS_KEY) } returns null
        coEvery { service.getLatestNews(date = "2024-04-29", count = 10) } throws Exception("idk")

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getLatestNews("2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult(LATEST_NEWS_KEY, 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(news)
        coVerify { service.getLatestNews(date = "2024-04-29", count = 10) }
        coVerify { observer.onChanged(Resource.error("idk", news)) }
        verify { rateLimiter.reset(LATEST_NEWS_KEY) }
    }

    @Test
    fun testGetNewsBySourceFromDb() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns false
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        coEvery { dao.findNewsSearchResult("foo") } returns null

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getNewsBySource("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        dbData.postValue(news)
        verify { observer.onChanged(Resource.success(news)) }
        verify { service wasNot Called }
    }

    @Test
    fun testGetNewsBySourceFromNetwork() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns true
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        coEvery { dao.findNewsSearchResult("foo") } returns null
        val response = NewsSearchResponse(NewsSearchResponse.Article(2, news))
        coEvery { service.getNewsBySource(source = "foo", count = 10) } returns response

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getNewsBySource("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(news)
        coVerify { service.getNewsBySource(source = "foo", count = 10) }
        coVerify { dao.insertNews(news) }
        coVerify { dao.insertNewsSearchResult(searchResult) }
        verify { observer.onChanged(Resource.success(news)) }
    }

    @Test
    fun testGetNewsBySourceFromNetworkError() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns true
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        coEvery { dao.findNewsSearchResult("foo") } returns null
        coEvery { service.getNewsBySource(source = "foo", count = 10) } throws Exception("idk")

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getNewsBySource("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(news)
        coVerify { service.getNewsBySource(source = "foo", count = 10) }
        coVerify { observer.onChanged(Resource.error("idk", news)) }
        verify { rateLimiter.reset("foo") }
    }

    @Test
    fun testGetNewsByCategoryFromDb() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns false
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        coEvery { dao.findNewsSearchResult("foo") } returns null

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getNewsByCategory("foo", "2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        dbData.postValue(news)
        verify { observer.onChanged(Resource.success(news)) }
        verify { service wasNot Called }
    }

    @Test
    fun testGetNewsByCategoryFromNetwork() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns true
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        coEvery { dao.findNewsSearchResult("foo") } returns null
        val response = NewsSearchResponse(NewsSearchResponse.Article(2, news))
        coEvery {
            service.getNewsByCategory(
                category = "foo",
                date = "2024-04-29",
                count = 10
            )
        } returns response

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getNewsByCategory("foo", "2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(news)
        coVerify { service.getNewsByCategory(category = "foo", date = "2024-04-29", count = 10) }
        coVerify { dao.insertNews(news) }
        coVerify { dao.insertNewsSearchResult(searchResult) }
        verify { observer.onChanged(Resource.success(news)) }
    }

    @Test
    fun testGetNewsByCategoryFromNetworkError() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns true
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        coEvery { dao.findNewsSearchResult("foo") } returns null
        coEvery {
            service.getNewsByCategory(
                category = "foo",
                date = "2024-04-29",
                count = 10
            )
        } throws Exception("idk")

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.getNewsByCategory("foo", "2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(news)
        coVerify { service.getNewsByCategory(category = "foo", date = "2024-04-29", count = 10) }
        coVerify { observer.onChanged(Resource.error("idk", news)) }
        verify { rateLimiter.reset("foo") }
    }

    @Test
    fun testGetCategoryNextPageNull() = runTest {
        coEvery { dao.findNewsSearchResult("foo") } returns null
        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.getCategoryNextPage("foo", "2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(null) }
    }

    @Test
    fun testGetCategoryNextPageFalse() = runTest {
        val ids = listOf("0", "1")
        val searchResult = NewsSearchResult("foo", 2, ids)
        coEvery { dao.findNewsSearchResult("foo") } returns searchResult
        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.getCategoryNextPage("foo", "2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.success(false)) }
    }

    @Test
    fun testGetCategoryNextPageTrue() = runTest {
        val ids = listOf("0", "1")
        val searchResult = NewsSearchResult("foo", 10, ids)
        coEvery { dao.findNewsSearchResult("foo") } returns searchResult
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        val response = NewsSearchResponse(NewsSearchResponse.Article(2, news))
        coEvery {
            service.getNewsByCategory(
                category = "foo",
                date = "2024-04-29",
                count = 10,
                page = 2
            )
        } returns response

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.getCategoryNextPage("foo", "2024-04-29", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.success(true)) }
    }

    @Test
    fun testSearchFromDb() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns false
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        coEvery { dao.findNewsSearchResult("foo") } returns null

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.searchNews("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        dbData.postValue(news)
        verify { observer.onChanged(Resource.success(news)) }
        verify { service wasNot Called }
    }

    @Test
    fun testSearchFromNetwork() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns true
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        coEvery { dao.findNewsSearchResult("foo") } returns null
        val response = NewsSearchResponse(NewsSearchResponse.Article(2, news))
        coEvery { service.searchNews(keyword = "foo", count = 10) } returns response

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.searchNews("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(news)
        coVerify { service.searchNews(keyword = "foo", count = 10) }
        coVerify { dao.insertNews(news) }
        coVerify { dao.insertNewsSearchResult(searchResult) }
        verify { observer.onChanged(Resource.success(news)) }
    }

    @Test
    fun testSearchFromNetworkError() = runTest {
        every { rateLimiter.shouldFetch("foo") } returns true
        val ids = listOf("0", "1")
        val dbSearchResult = MutableLiveData<NewsSearchResult>()
        every { dao.getNewsSearchResult("foo") } returns dbSearchResult
        val dbData = MutableLiveData<List<News>>()
        every { dao.getNewsByOrder(ids) } returns dbData
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        coEvery { dao.findNewsSearchResult("foo") } returns null
        coEvery { service.searchNews(keyword = "foo", count = 10) } throws Exception("idk")

        val observer = mockk<Observer<Resource<List<News>>>>(relaxed = true)
        repository.searchNews("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val searchResult = NewsSearchResult("foo", 2, ids)
        dbSearchResult.postValue(searchResult)
        dbData.postValue(news)
        coVerify { service.searchNews(keyword = "foo", count = 10) }
        coVerify { observer.onChanged(Resource.error("idk", news)) }
        verify { rateLimiter.reset("foo") }
    }

    @Test
    fun testSearchNextPageNull() = runTest {
        coEvery { dao.findNewsSearchResult("foo") } returns null
        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.searchNewsNextPage("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(null) }
    }

    @Test
    fun testSearchNextPageFalse() = runTest {
        val ids = listOf("0", "1")
        val searchResult = NewsSearchResult("foo", 2, ids)
        coEvery { dao.findNewsSearchResult("foo") } returns searchResult
        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.searchNewsNextPage("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.success(false)) }
    }

    @Test
    fun testSearchNextPageTrue() = runTest {
        val ids = listOf("0", "1")
        val searchResult = NewsSearchResult("foo", 10, ids)
        coEvery { dao.findNewsSearchResult("foo") } returns searchResult
        val news = TestUtil.createNews(2, "foo", "bar", "src")
        val response = NewsSearchResponse(NewsSearchResponse.Article(2, news))
        coEvery { service.searchNews(keyword = "foo", count = 10, page = 2) } returns response

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        repository.searchNewsNextPage("foo", 10).observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.success(true)) }
    }

}