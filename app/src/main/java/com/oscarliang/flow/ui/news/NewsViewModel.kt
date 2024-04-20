package com.oscarliang.flow.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.oscarliang.flow.model.Category
import com.oscarliang.flow.model.News
import com.oscarliang.flow.repository.CategoryRepository
import com.oscarliang.flow.repository.NewsRepository
import com.oscarliang.flow.util.AbsentLiveData
import com.oscarliang.flow.util.LoadMoreState
import com.oscarliang.flow.util.NextPageHandler
import com.oscarliang.flow.util.Resource
import kotlinx.coroutines.launch

class NewsViewModel(
    private val newsRepository: NewsRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _query = MutableLiveData<Query>()
    val query: LiveData<Query>
        get() = _query

    private val categoryHandler = CategoryHandler(categoryRepository)
    private val _categoryState = categoryHandler.categoryState
    val categoryState: LiveData<CategoryState>
        get() = _categoryState

    val latestNews: LiveData<Resource<List<News>>> = _query.switchMap { input ->
        input.ifExists { date, count ->
            newsRepository.getLatestNews(
                date = date,
                count = count
            )
        }
    }
    val news: LiveData<Resource<List<News>>> = _categoryState.switchMap { input ->
        input.ifExists { category, date, count ->
            newsRepository.searchCategory(
                category = category,
                date = date,
                count = count
            )
        }
    }
    val categories: LiveData<Resource<List<Category>>> = categoryRepository.getCategories()

    private val nextPageHandler = NextPageHandler()
    val loadMoreState: LiveData<LoadMoreState>
        get() = nextPageHandler.loadMoreState

    fun setQuery(date: String, count: Int) {
        val update = Query(date, count)
        if (_query.value == update) {
            return
        }
        nextPageHandler.reset()
        categoryHandler.setQuery(update)
        _query.value = update
    }

    fun loadNextPage() {
        _categoryState.value?.let {
            if (it.category.isNotBlank()) {
                nextPageHandler.queryNextPage {
                    newsRepository.searchCategoryNextPage(
                        it.category,
                        it.query.date,
                        it.query.count
                    )
                }
            }
        }
    }

    fun refresh() {
        _query.value?.let {
            _query.value = it
        }
        _categoryState.value?.let {
            _categoryState.value = it
        }
    }

    fun selectCategory(category: Category) {
        if (category.isSelected) {
            return
        }
        nextPageHandler.reset()
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun toggleBookmark(news: News) {
        val current = news.bookmark
        val updated = news.copy(bookmark = !current)
        viewModelScope.launch {
            newsRepository.updateNews(updated)
        }
    }

    data class Query(
        val date: String,
        val count: Int
    ) {
        fun <T> ifExists(f: (String, Int) -> LiveData<T>): LiveData<T> {
            return if (date.isBlank()) {
                AbsentLiveData.create()
            } else {
                f(date, count)
            }
        }
    }

    data class CategoryState(
        val category: String,
        val query: Query
    ) {
        fun <T> ifExists(f: (String, String, Int) -> LiveData<T>): LiveData<T> {
            return if (category.isBlank()) {
                AbsentLiveData.create()
            } else {
                f(category, query.date, query.count)
            }
        }
    }

    class CategoryHandler(
        private val categoryRepository: CategoryRepository
    ) : Observer<Category?> {

        val categoryState = MutableLiveData<CategoryState>()
        private var selectedCategory: LiveData<Category>? = null
        private var query: Query? = null

        init {
            reset()
        }

        fun setQuery(query: Query) {
            if (this.query == query) {
                return
            }
            reset()
            this.query = query
            selectedCategory = categoryRepository.getSelectedCategory()
            selectedCategory?.observeForever(this)
        }

        override fun onChanged(value: Category?) {
            value?.let {
                categoryState.setValue(
                    CategoryState(
                        category = value.id,
                        query = query!!
                    )
                )
            }
        }

        private fun reset() {
            selectedCategory?.removeObserver(this)
            selectedCategory = null
        }

    }

}