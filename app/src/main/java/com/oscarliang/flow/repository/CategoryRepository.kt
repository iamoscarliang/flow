package com.oscarliang.flow.repository

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.oscarliang.flow.db.CategoryDao
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.model.Category
import com.oscarliang.flow.util.NetworkBoundResource
import com.oscarliang.flow.util.Resource

class CategoryRepository(
    private val db: NewsDatabase,
    private val categoryDao: CategoryDao
) {

    fun getCategories(): LiveData<Resource<List<Category>>> {
        return object : NetworkBoundResource<List<Category>, List<Category>>() {
            override suspend fun query(): List<Category> {
                return categoryDao.findCategories()
            }

            override fun queryObservable(): LiveData<List<Category>> {
                return categoryDao.getCategories()
            }

            override suspend fun fetch(): List<Category> {
                return listOf(
                    Category("news/Business", "Business", true),
                    Category("news/Politics", "Politics"),
                    Category("news/Technology", "Technology"),
                    Category("news/Environment", "Environment"),
                    Category("news/Health", "Health"),
                    Category("news/Science", "Science"),
                    Category("news/Sports", "Sports"),
                    Category("news/Arts_and_Entertainment", "Arts_and_Entertainment")
                )
            }

            override suspend fun saveFetchResult(data: List<Category>) {
                categoryDao.insertCategories(data)
            }

            override fun shouldFetch(data: List<Category>): Boolean {
                return data.isEmpty()
            }
        }.asLiveData()
    }

    fun getSelectedCategory(): LiveData<Category> {
        return categoryDao.getSelectedCategory()
    }

    suspend fun updateCategory(category: Category) {
        db.withTransaction {
            categoryDao.setSelectedCategory(category.id)
            categoryDao.clearSelectedCategory(category.id)
        }
    }

}