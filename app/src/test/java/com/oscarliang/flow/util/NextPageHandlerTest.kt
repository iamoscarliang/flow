package com.oscarliang.flow.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NextPageHandlerTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var nextPageHandler: NextPageHandler

    @Before
    fun init() {
        nextPageHandler = NextPageHandler()
    }

    @Test
    fun testSearchNextPageNull() {
        val observer = mockk<Observer<LoadMoreState>>(relaxed = true)
        nextPageHandler.loadMoreState.observeForever(observer)

        val nextPageResult = MutableLiveData<Resource<Boolean>?>()
        nextPageHandler.queryNextPage {
            nextPageResult
        }
        verify {
            observer.onChanged(
                LoadMoreState(
                    isRunning = true,
                    hasMore = true,
                    errorMessage = null
                )
            )
        }
        clearMocks(observer)

        nextPageResult.value = null
        assertEquals(nextPageHandler.hasMore, true)
        verify {
            observer.onChanged(
                LoadMoreState(
                    isRunning = false,
                    hasMore = true,
                    errorMessage = null
                )
            )
        }
    }

    @Test
    fun testSearchNextPageTrue() {
        val observer = mockk<Observer<LoadMoreState>>(relaxed = true)
        nextPageHandler.loadMoreState.observeForever(observer)

        val nextPageResult = MutableLiveData<Resource<Boolean>?>()
        nextPageHandler.queryNextPage {
            nextPageResult
        }
        verify {
            observer.onChanged(
                LoadMoreState(
                    isRunning = true,
                    hasMore = true,
                    errorMessage = null
                )
            )
        }
        clearMocks(observer)

        nextPageResult.value = Resource.success(true)
        assertEquals(nextPageHandler.hasMore, true)
        verify {
            observer.onChanged(
                LoadMoreState(
                    isRunning = false,
                    hasMore = true,
                    errorMessage = null
                )
            )
        }
    }

    @Test
    fun testSearchNextPageFalse() {
        val observer = mockk<Observer<LoadMoreState>>(relaxed = true)
        nextPageHandler.loadMoreState.observeForever(observer)

        val nextPageResult = MutableLiveData<Resource<Boolean>?>()
        nextPageHandler.queryNextPage {
            nextPageResult
        }
        verify {
            observer.onChanged(
                LoadMoreState(
                    isRunning = true,
                    hasMore = true,
                    errorMessage = null
                )
            )
        }
        clearMocks(observer)

        nextPageResult.value = Resource.success(false)
        assertEquals(nextPageHandler.hasMore, false)
        verify {
            observer.onChanged(
                LoadMoreState(
                    isRunning = false,
                    hasMore = false,
                    errorMessage = null
                )
            )
        }
    }

    @Test
    fun testSearchNextPageError() {
        val observer = mockk<Observer<LoadMoreState>>(relaxed = true)
        nextPageHandler.loadMoreState.observeForever(observer)

        val nextPageResult = MutableLiveData<Resource<Boolean>?>()
        nextPageHandler.queryNextPage {
            nextPageResult
        }
        verify {
            observer.onChanged(
                LoadMoreState(
                    isRunning = true,
                    hasMore = true,
                    errorMessage = null
                )
            )
        }
        clearMocks(observer)

        nextPageResult.value = Resource.error("idk", true)
        assertEquals(nextPageHandler.hasMore, true)
        verify {
            observer.onChanged(
                LoadMoreState(
                    isRunning = false,
                    hasMore = true,
                    errorMessage = "idk"
                )
            )
        }
    }

}