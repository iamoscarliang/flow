package com.oscarliang.flow.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class FetchNextSearchPageTaskTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var handleQuery: () -> Foo?

    private lateinit var handleFetchNextPage: (Foo) -> Unit

    private lateinit var handleShouldFetch: (Foo?) -> Boolean

    private lateinit var fetchNextSearchPageTask: FetchNextSearchPageTask<Foo>

    private data class Foo(var value: Int)

    @Before
    fun init() {
        fetchNextSearchPageTask = object : FetchNextSearchPageTask<Foo>() {
            override suspend fun query(): Foo? {
                return handleQuery()
            }

            override fun shouldFetch(data: Foo): Boolean {
                return handleShouldFetch(data)
            }

            override suspend fun fetchNextPage(data: Foo) {
                handleFetchNextPage(data)
            }

        }
    }

    @Test
    fun testSearchNextPageNull() = runTest {
        handleQuery = { null }

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        fetchNextSearchPageTask.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(null) }
    }

    @Test
    fun testSearchNextPageFalse() = runTest {
        val fetchedDbValue = Foo(1)
        handleQuery = { fetchedDbValue }
        handleShouldFetch = { it == null }

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        fetchNextSearchPageTask.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.success(false)) }
    }

    @Test
    fun testSearchNextPageTrue() = runTest {
        val saved = AtomicBoolean(false)
        val fetchedDbValue = Foo(1)
        handleQuery = { fetchedDbValue }
        handleShouldFetch = { it != null }
        handleFetchNextPage = {
            saved.set(true)
        }

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        fetchNextSearchPageTask.asLiveData().observeForever(observer)
        advanceUntilIdle()
        assertEquals(saved.get(), true)
        verify { observer.onChanged(Resource.success(true)) }
    }

    @Test
    fun testSearchNextPageTrueError() = runTest {
        val fetchedDbValue = Foo(1)
        handleQuery = { fetchedDbValue }
        handleShouldFetch = { it != null }
        handleFetchNextPage = {
            throw Exception("idk")
        }

        val observer = mockk<Observer<Resource<Boolean>?>>(relaxed = true)
        fetchNextSearchPageTask.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.error("idk", true)) }
    }

}