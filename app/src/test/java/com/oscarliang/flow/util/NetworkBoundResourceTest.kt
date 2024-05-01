package com.oscarliang.flow.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.mockk.clearMocks
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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class NetworkBoundResourceTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var handleQuery: () -> Foo?

    private lateinit var handleFetch: () -> Foo

    private lateinit var handleSaveFetchResult: (Foo) -> Unit

    private lateinit var handleShouldFetch: (Foo?) -> Boolean

    private val dbData = MutableLiveData<Foo>()

    private lateinit var networkBoundResource: NetworkBoundResource<Foo, Foo>

    @Before
    fun init() {
        networkBoundResource = object : NetworkBoundResource<Foo, Foo>() {
            override suspend fun query(): Foo? {
                return handleQuery()
            }

            override fun queryObservable(): LiveData<Foo> {
                return dbData
            }

            override suspend fun fetch(): Foo {
                return handleFetch()
            }

            override suspend fun saveFetchResult(data: Foo) {
                handleSaveFetchResult(data)
            }

            override fun shouldFetch(data: Foo?): Boolean {
                return handleShouldFetch(data)
            }
        }
    }

    @Test
    fun testFetchFromDb() = runTest {
        val saved = AtomicBoolean(false)
        val fetchedDbValue = Foo(1)
        handleQuery = { fetchedDbValue }
        handleShouldFetch = { it == null }
        handleSaveFetchResult = {
            saved.set(true)
        }

        val observer = mockk<Observer<Resource<Foo>>>(relaxed = true)
        networkBoundResource.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        dbData.value = fetchedDbValue
        assertEquals(saved.get(), false)
        verify { observer.onChanged(Resource.success(fetchedDbValue)) }
    }

    @Test
    fun testFetchFromNetwork() = runTest {
        val saved = AtomicReference<Foo>()
        handleQuery = { null }
        handleShouldFetch = { it == null }
        val networkResult = Foo(1)
        handleFetch = { networkResult }
        handleSaveFetchResult = { foo ->
            saved.set(foo)
        }

        val observer = mockk<Observer<Resource<Foo>>>(relaxed = true)
        networkBoundResource.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val fetchedDbValue = Foo(1)
        dbData.value = fetchedDbValue
        assertEquals(saved.get(), networkResult)
        verify { observer.onChanged(Resource.success(fetchedDbValue)) }
    }

    @Test
    fun testFetchFromNetworkError() = runTest {
        val saved = AtomicBoolean(false)
        handleQuery = { null }
        handleShouldFetch = { it == null }
        handleFetch = { throw Exception("idk") }
        handleSaveFetchResult = {
            saved.set(true)
        }

        val observer = mockk<Observer<Resource<Foo>>>(relaxed = true)
        networkBoundResource.asLiveData().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        dbData.value = null
        assertEquals(saved.get(), false)
        verify { observer.onChanged(Resource.error("idk", null)) }
    }

    private data class Foo(var value: Int)

}