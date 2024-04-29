package com.oscarliang.flow.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.oscarliang.flow.util.API_KEY
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class NewsServiceTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: NewsService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun testGetLatestNews() = runTest {
        enqueueResponse("latest.json")
        val response = service.getLatestNews(
            date = "2024-04-29",
            count = 10
        )

        val request = mockWebServer.takeRequest()
        assertEquals(
            request.path,
            "/api/v1/article/getArticles?apiKey=$API_KEY&lang=eng&dateStart=2024-04-29&articlesCount=10"
        )

        assertNotNull(response)
        assertEquals(response.article.total, 44084)
        assertEquals(response.article.news.size, 10)

        val news1 = response.article.news[0]
        assertEquals(news1.id, "8102512044")
        assertEquals(
            news1.title,
            "What to watch"
        )
        assertEquals(news1.source?.title, "HeraldCourier.com")

        val news2 = response.article.news[1]
        assertEquals(news2.id, "8102514179")
        assertEquals(
            news2.title,
            "Barings Emerging EMEA Opportunities Plc - Net Asset Value(s)"
        )
        assertEquals(news2.source?.title, "FinanzNachrichten.de")
    }

    @Test
    fun testGetNewsBySource() = runTest {
        enqueueResponse("source.json")
        val response = service.getNewsBySource(
            source = "yahoo.com",
            count = 10
        )

        val request = mockWebServer.takeRequest()
        assertEquals(
            request.path,
            "/api/v1/article/getArticles?apiKey=$API_KEY&lang=eng&sourceUri=yahoo.com&articlesCount=10"
        )

        assertNotNull(response)
        assertEquals(response.article.total, 80068)
        assertEquals(response.article.news.size, 10)

        val news1 = response.article.news[0]
        assertEquals(news1.id, "8102522569")
        assertEquals(
            news1.title,
            "P.E.I. farmer worries stray voltage is harming his cattle"
        )
        assertEquals(news1.source?.title, "Yahoo")

        val news2 = response.article.news[1]
        assertEquals(news2.id, "8102522580")
        assertEquals(
            news2.title,
            "Lawmakers to Ohio students: Screen time's over, kids"
        )
        assertEquals(news2.source?.title, "Yahoo")
    }

    @Test
    fun testGetNewsByCategory() = runTest {
        enqueueResponse("category.json")
        val response = service.getNewsByCategory(
            category = "news/Sports",
            date = "2024-04-29",
            count = 10
        )

        val request = mockWebServer.takeRequest()
        assertEquals(
            request.path,
            "/api/v1/article/getArticles?apiKey=$API_KEY&lang=eng&categoryUri=news%2FSports&dateStart=2024-04-29&articlesCount=10&articlesPage=1"
        )

        assertNotNull(response)
        assertEquals(response.article.total, 6537)
        assertEquals(response.article.news.size, 10)

        val news1 = response.article.news[0]
        assertEquals(news1.id, "8102465986")
        assertEquals(
            news1.title,
            "F1 driver market 'going to erupt very soon' with established drivers to be axed"
        )
        assertEquals(news1.source?.title, "EXPRESS")

        val news2 = response.article.news[1]
        assertEquals(news2.id, "8102465329")
        assertEquals(
            news2.title,
            "'I showed myself that I could do that': Brayden Nichols' triumphant return to Hallsville baseball"
        )
        assertEquals(news2.source?.title, "Yahoo Sports")
    }

    @Test
    fun testSearchNews() = runTest {
        enqueueResponse("search.json")
        val response = service.searchNews(
            keyword = "android",
            count = 10
        )

        val request = mockWebServer.takeRequest()
        assertEquals(
            request.path,
            "/api/v1/article/getArticles?apiKey=$API_KEY&lang=eng&forceMaxDataTimeWindow=7&keyword=android&articlesCount=10&articlesPage=1"
        )

        assertNotNull(response)
        assertEquals(response.article.total, 25256)
        assertEquals(response.article.news.size, 10)

        val news1 = response.article.news[0]
        assertEquals(news1.id, "8102450186")
        assertEquals(
            news1.title,
            "PlayboxTV and Shucae Films Collaborate to Redefine the entertainment horizon for viewers"
        )
        assertEquals(news1.source?.title, "Sri Lanka Source")

        val news2 = response.article.news[1]
        assertEquals(news2.id, "8102449760")
        assertEquals(
            news2.title,
            "Nothing Phone (2a) India-exclusive Blue Edition launched starting at ₹19,999"
        )
        assertEquals(news2.source?.title, "Business Insider India")
    }

    private fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader!!
            .getResourceAsStream("api-response/$fileName")
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer.enqueue(
            mockResponse
                .setBody(source.readString(Charsets.UTF_8))
        )
    }

}