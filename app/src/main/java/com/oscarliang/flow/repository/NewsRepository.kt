package com.oscarliang.flow.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.room.withTransaction
import com.oscarliang.flow.api.NewsSearchResponse
import com.oscarliang.flow.api.NewsService
import com.oscarliang.flow.db.NewsDao
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.model.News
import com.oscarliang.flow.model.NewsSearchResult
import com.oscarliang.flow.util.AbsentLiveData
import com.oscarliang.flow.util.FetchNextSearchPageTask
import com.oscarliang.flow.util.NetworkBoundResource
import com.oscarliang.flow.util.RateLimiter
import com.oscarliang.flow.util.Resource

class NewsRepository(
    private val db: NewsDatabase,
    private val newsDao: NewsDao,
    private val service: NewsService,
    private val rateLimiter: RateLimiter<String>
) {

    fun getLatestNews(
        date: String,
        count: Int
    ): LiveData<Resource<List<News>>> {
        return object : NetworkBoundResource<List<News>, NewsSearchResponse>() {
            override suspend fun query(): List<News> {
                val result = newsDao.findNewsSearchResult(LATEST_NEWS_KEY)
                return if (result == null) {
                    listOf()
                } else {
                    newsDao.findNewsById(result.newsIds)
                }
            }

            override fun queryObservable(): LiveData<List<News>> {
                return newsDao.getNewsSearchResult(LATEST_NEWS_KEY).switchMap { searchData ->
                    if (searchData == null) {
                        AbsentLiveData.create()
                    } else {
                        newsDao.getNewsByOrder(searchData.newsIds)
                    }
                }
            }

            override suspend fun fetch(): NewsSearchResponse {
                return service.getLatestNews(
                    date = date,
                    count = count
                )
            }

            override suspend fun saveFetchResult(data: NewsSearchResponse) {
                val news = data.article.news
                updateBookmarks(news, newsDao.findBookmarks())

                val newsIds = news.map { it.id }
                val result = NewsSearchResult(
                    query = LATEST_NEWS_KEY,
                    total = data.article.total,
                    newsIds = newsIds
                )
                db.withTransaction {
                    newsDao.insertNews(news)
                    newsDao.insertNewsSearchResult(result)
                }
            }

            override fun shouldFetch(data: List<News>?): Boolean {
                return rateLimiter.shouldFetch(LATEST_NEWS_KEY)
            }

            override fun onFetchFailed(exception: Exception) {
                rateLimiter.reset(LATEST_NEWS_KEY)
            }
        }.asLiveData()
    }

    fun getNewsBySource(
        source: String,
        count: Int
    ): LiveData<Resource<List<News>>> {
        return object : NetworkBoundResource<List<News>, NewsSearchResponse>() {
            override suspend fun query(): List<News> {
                val result = newsDao.findNewsSearchResult(source)
                return if (result == null) {
                    listOf()
                } else {
                    newsDao.findNewsById(result.newsIds)
                }
            }

            override fun queryObservable(): LiveData<List<News>> {
                return newsDao.getNewsSearchResult(source).switchMap { searchData ->
                    if (searchData == null) {
                        AbsentLiveData.create()
                    } else {
                        newsDao.getNewsByOrder(searchData.newsIds)
                    }
                }
            }

            override suspend fun fetch(): NewsSearchResponse {
                return service.getNewsBySource(
                    source = source,
                    count = count
                )
            }

            override suspend fun saveFetchResult(data: NewsSearchResponse) {
                val news = data.article.news
                updateBookmarks(news, newsDao.findBookmarks())

                val newsIds = news.map { it.id }
                val result = NewsSearchResult(
                    query = source,
                    total = data.article.total,
                    newsIds = newsIds
                )
                db.withTransaction {
                    newsDao.insertNews(news)
                    newsDao.insertNewsSearchResult(result)
                }
            }

            override fun shouldFetch(data: List<News>?): Boolean {
                return rateLimiter.shouldFetch(source)
            }

            override fun onFetchFailed(exception: Exception) {
                rateLimiter.reset(source)
            }
        }.asLiveData()
    }

    fun getNewsByCategory(
        category: String,
        date: String,
        count: Int
    ): LiveData<Resource<List<News>>> {
        return object : NetworkBoundResource<List<News>, NewsSearchResponse>() {
            override suspend fun query(): List<News> {
                val result = newsDao.findNewsSearchResult(category)
                return if (result == null) {
                    listOf()
                } else {
                    newsDao.findNewsById(result.newsIds)
                }
            }

            override fun queryObservable(): LiveData<List<News>> {
                return newsDao.getNewsSearchResult(category).switchMap { result ->
                    if (result == null) {
                        AbsentLiveData.create()
                    } else {
                        newsDao.getNewsByOrder(result.newsIds)
                    }
                }
            }

            override suspend fun fetch(): NewsSearchResponse {
                return service.getNewsByCategory(
                    category = category,
                    date = date,
                    count = count
                )
            }

            override suspend fun saveFetchResult(data: NewsSearchResponse) {
                val news = data.article.news
                updateBookmarks(news, newsDao.findBookmarks())

                val newsIds = news.map { it.id }
                val result = NewsSearchResult(
                    query = category,
                    total = data.article.total,
                    newsIds = newsIds
                )
                db.withTransaction {
                    newsDao.insertNews(news)
                    newsDao.insertNewsSearchResult(result)
                }
            }

            override fun shouldFetch(data: List<News>?): Boolean {
                return rateLimiter.shouldFetch(category)
            }

            override fun onFetchFailed(exception: Exception) {
                rateLimiter.reset(category)
            }
        }.asLiveData()
    }

    fun getCategoryNextPage(
        category: String,
        date: String,
        count: Int
    ): LiveData<Resource<Boolean>?> {
        return object : FetchNextSearchPageTask<NewsSearchResult?>() {
            override suspend fun query(): NewsSearchResult? {
                return newsDao.findNewsSearchResult(category)
            }

            override fun shouldFetch(data: NewsSearchResult?): Boolean {
                val current = data!!.newsIds.size
                return current < data.total
            }

            override suspend fun fetchNextPage(data: NewsSearchResult?) {
                val current = data!!.newsIds.size
                val response = service.getNewsByCategory(
                    category = category,
                    date = date,
                    count = count,
                    page = current / count + 1
                )
                val news = response.article.news
                updateBookmarks(news, newsDao.findBookmarks())

                // We merge all new search result into current result list
                val newsIds = mutableListOf<String>()
                newsIds.addAll(data.newsIds)
                newsIds.addAll(news.map { it.id })
                val merged = NewsSearchResult(
                    query = category,
                    total = response.article.total,
                    newsIds = newsIds
                )
                db.withTransaction {
                    newsDao.insertNews(news)
                    newsDao.insertNewsSearchResult(merged)
                }
            }
        }.asLiveData()
    }

    fun searchNews(
        keyword: String,
        count: Int
    ): LiveData<Resource<List<News>>> {
        return object : NetworkBoundResource<List<News>, NewsSearchResponse>() {
            override suspend fun query(): List<News> {
                val result = newsDao.findNewsSearchResult(keyword)
                return if (result == null) {
                    listOf()
                } else {
                    newsDao.findNewsById(result.newsIds)
                }
            }

            override fun queryObservable(): LiveData<List<News>> {
                return newsDao.getNewsSearchResult(keyword).switchMap { result ->
                    if (result == null) {
                        AbsentLiveData.create()
                    } else {
                        newsDao.getNewsByOrder(result.newsIds)
                    }
                }
            }

            override suspend fun fetch(): NewsSearchResponse {
                return service.searchNews(
                    keyword = keyword,
                    count = count
                )
            }

            override suspend fun saveFetchResult(data: NewsSearchResponse) {
                val news = data.article.news
                updateBookmarks(news, newsDao.findBookmarks())

                val newsIds = news.map { it.id }
                val result = NewsSearchResult(
                    query = keyword,
                    total = data.article.total,
                    newsIds = newsIds
                )
                db.withTransaction {
                    newsDao.insertNews(news)
                    newsDao.insertNewsSearchResult(result)
                }
            }

            override fun shouldFetch(data: List<News>?): Boolean {
                return rateLimiter.shouldFetch(keyword)
            }

            override fun onFetchFailed(exception: Exception) {
                rateLimiter.reset(keyword)
            }
        }.asLiveData()
    }

    fun searchNewsNextPage(
        keyword: String,
        count: Int
    ): LiveData<Resource<Boolean>?> {
        return object : FetchNextSearchPageTask<NewsSearchResult?>() {
            override suspend fun query(): NewsSearchResult? {
                return newsDao.findNewsSearchResult(keyword)
            }

            override fun shouldFetch(data: NewsSearchResult?): Boolean {
                val current = data!!.newsIds.size
                return current < data.total
            }

            override suspend fun fetchNextPage(data: NewsSearchResult?) {
                val current = data!!.newsIds.size
                val response = service.searchNews(
                    keyword = keyword,
                    count = count,
                    page = current / count + 1
                )
                val news = response.article.news
                updateBookmarks(news, newsDao.findBookmarks())

                // We merge all new search result into current result list
                val newsIds = mutableListOf<String>()
                newsIds.addAll(data.newsIds)
                newsIds.addAll(news.map { it.id })
                val merged = NewsSearchResult(
                    query = keyword,
                    total = response.article.total,
                    newsIds = newsIds
                )
                db.withTransaction {
                    newsDao.insertNews(news)
                    newsDao.insertNewsSearchResult(merged)
                }
            }
        }.asLiveData()
    }

    fun getBookmarks(): LiveData<List<News>> {
        return newsDao.getBookmarks()
    }

    fun getNewsById(id: String): LiveData<News> {
        return newsDao.getNewsById(id)
    }

    suspend fun updateNews(news: News) {
        newsDao.updateNews(news)
    }

    private fun updateBookmarks(news: List<News>, bookmarks: List<News>) {
        news.forEach { newData ->
            // Prevent overriding bookmark field
            newData.bookmark = bookmarks.any { currentData ->
                currentData.id == newData.id
            }
        }
    }

    companion object {
        const val LATEST_NEWS_KEY = "latest"
    }

}