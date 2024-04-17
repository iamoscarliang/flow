package com.oscarliang.flow.util

import androidx.lifecycle.liveData
import androidx.room.withTransaction
import com.oscarliang.flow.api.NewsService
import com.oscarliang.flow.db.NewsDao
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.model.NewsSearchResult

class FetchNextSearchPageTask(
    private val query: String,
    private val language: String,
    private val number: Int,
    private val db: NewsDatabase,
    private val newsDao: NewsDao,
    private val service: NewsService
) {

    fun asLiveData() = liveData {
        val current = newsDao.findNewsSearchResult(query)
        if (current == null) {
            emit(null)
            return@liveData
        }
        val count = current.newsIds.size
        if (count >= current.available) {
            emit(Resource.success(false))
            return@liveData
        }

        try {
            val response = service.searchNews(
                query = query,
                language = language,
                number = number,
                offset = count
            )
            val news = response.news
            val bookmarks = newsDao.findBookmarks()
            news.forEach { newData ->
                // We prevent overriding bookmark field
                newData.bookmark = bookmarks.any { currentData ->
                    currentData.id == newData.id
                }
            }

            // We merge all new search result into current result list
            val newsIds = mutableListOf<Int>()
            newsIds.addAll(current.newsIds)
            newsIds.addAll(news.map { it.id })
            val merged = NewsSearchResult(
                query = query,
                available = response.available,
                newsIds = newsIds
            )
            db.withTransaction {
                newsDao.insertNews(news)
                newsDao.insertNewsSearchResult(merged)
            }
            emit(Resource.success(true))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", true))
        }
    }

}