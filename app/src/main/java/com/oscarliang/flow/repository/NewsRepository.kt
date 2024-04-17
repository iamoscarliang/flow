package com.oscarliang.flow.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.room.withTransaction
import com.oscarliang.flow.api.NewsSearchResponse
import com.oscarliang.flow.api.NewsService
import com.oscarliang.flow.db.NewsDao
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.model.LatestNewsResult
import com.oscarliang.flow.model.News
import com.oscarliang.flow.model.NewsSearchResult
import com.oscarliang.flow.util.AbsentLiveData
import com.oscarliang.flow.util.FetchNextSearchPageTask
import com.oscarliang.flow.util.NetworkBoundResource
import com.oscarliang.flow.util.Resource

class NewsRepository(
    private val db: NewsDatabase,
    private val newsDao: NewsDao,
    private val service: NewsService
) {

    fun getLatestNews(
        language: String,
        number: Int
    ): LiveData<Resource<List<News>>> {
        return object : NetworkBoundResource<List<News>, NewsSearchResponse>() {
            override suspend fun query(): List<News> {
                val result = newsDao.findLatestNewsResult(language)
                return if (result == null) {
                    listOf()
                } else {
                    newsDao.findNewsById(result.newsIds)
                }
            }

            override fun queryObservable(): LiveData<List<News>> {
                return newsDao.getLatestNewsResult(language).switchMap { searchData ->
                    if (searchData == null) {
                        AbsentLiveData.create()
                    } else {
                        newsDao.getNewsByOrder(searchData.newsIds)
                    }
                }
            }

            override suspend fun fetch(): NewsSearchResponse {
                return service.getLatestNews(
                    language = language,
                    number = number
                )
            }

            override suspend fun saveFetchResult(data: NewsSearchResponse) {
                val news = data.news
                val bookmarks = newsDao.findBookmarks()
                news.forEach { newData ->
                    // We prevent overriding bookmark field
                    newData.bookmark = bookmarks.any { currentData ->
                        currentData.id == newData.id
                    }
                }
                val newsIds = news.map { it.id }
                val latestNewsResult = LatestNewsResult(
                    query = language,
                    newsIds = newsIds
                )
                db.withTransaction {
                    newsDao.insertNews(news)
                    newsDao.insertLatestNewsResult(latestNewsResult)
                }
            }
        }.asLiveData()
    }

    fun searchNews(
        query: String,
        language: String,
        number: Int
    ): LiveData<Resource<List<News>>> {
        return object : NetworkBoundResource<List<News>, NewsSearchResponse>() {
            override suspend fun query(): List<News> {
                val result = newsDao.findNewsSearchResult(query)
                return if (result == null) {
                    listOf()
                } else {
                    newsDao.findNewsById(result.newsIds)
                }
            }

            override fun queryObservable(): LiveData<List<News>> {
                return newsDao.getNewsSearchResult(language).switchMap { searchData ->
                    if (searchData == null) {
                        AbsentLiveData.create()
                    } else {
                        newsDao.getNewsByOrder(searchData.newsIds)
                    }
                }
            }

            override suspend fun fetch(): NewsSearchResponse {
                return service.searchNews(
                    query = query,
                    language = language,
                    number = number
                )
            }

            override suspend fun saveFetchResult(data: NewsSearchResponse) {
                val news = data.news
                val bookmarks = newsDao.findBookmarks()
                news.forEach { newData ->
                    // We prevent overriding bookmark field
                    newData.bookmark = bookmarks.any { currentData ->
                        currentData.id == newData.id
                    }
                }
                val newsIds = news.map { it.id }
                val newsSearchResult = NewsSearchResult(
                    query = language,
                    available = data.available,
                    newsIds = newsIds
                )
                db.withTransaction {
                    newsDao.insertNews(news)
                    newsDao.insertNewsSearchResult(newsSearchResult)
                }
            }
        }.asLiveData()
    }

    fun searchNextPage(
        query: String,
        language: String,
        number: Int
    ): LiveData<Resource<Boolean>?> {
        return FetchNextSearchPageTask(
            query = query,
            language = language,
            number = number,
            db = db,
            newsDao = newsDao,
            service = service
        ).asLiveData()
    }

    fun getBookmarks(): LiveData<List<News>> {
        return newsDao.getBookmarks()
    }

    fun getNewsById(id: Int): LiveData<News> {
        return newsDao.getNewsById(id)
    }

    suspend fun updateNews(news: News) {
        newsDao.updateNews(news)
    }

}