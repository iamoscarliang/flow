package com.oscarliang.flow.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.flow.db.CategoryDao
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.model.Category
import com.oscarliang.flow.util.MainDispatcherRule
import com.oscarliang.flow.util.Resource
import com.oscarliang.flow.util.TestUtil
import io.mockk.clearMocks
import io.mockk.coEvery
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class CategoryRepositoryTest {

    @Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dao = mockk<CategoryDao>(relaxed = true)
    private lateinit var repository: CategoryRepository

    @Before
    fun init() {
        val db = mockk<NewsDatabase>(relaxed = true)
        every { db.categoryDao() } returns dao
        repository = CategoryRepository(
            db = db,
            categoryDao = dao
        )
    }

    @Test
    fun testGetCategoriesEmpty() = runTest {
        val dbData = MutableLiveData<List<Category>>()
        every { dao.getCategories() } returns dbData
        coEvery { dao.findCategories() } returns listOf()

        val observer = mockk<Observer<Resource<List<Category>>>>(relaxed = true)
        repository.getCategories().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        val categories = TestUtil.createCategories(2, "foo")
        dbData.postValue(categories)
        verify { observer.onChanged(Resource.success(categories)) }
    }

    @Test
    fun testGetCategoriesWithData() = runTest {
        val dbData = MutableLiveData<List<Category>>()
        every { dao.getCategories() } returns dbData
        val categories = TestUtil.createCategories(2, "foo")
        coEvery { dao.findCategories() } returns categories

        val observer = mockk<Observer<Resource<List<Category>>>>(relaxed = true)
        repository.getCategories().observeForever(observer)
        advanceUntilIdle()
        verify { observer.onChanged(Resource.loading(null)) }
        clearMocks(observer)

        dbData.postValue(categories)
        verify { observer.onChanged(Resource.success(categories)) }
    }

}