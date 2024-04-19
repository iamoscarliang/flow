package com.oscarliang.flow.util

import androidx.lifecycle.liveData
import androidx.room.withTransaction
import com.oscarliang.flow.api.NewsService
import com.oscarliang.flow.db.NewsDao
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.model.NewsSearchResult

class FetchNextSearchPageTask(
    private val category: String,
    private val date: String,
    private val count: Int,
    private val db: NewsDatabase,
    private val newsDao: NewsDao,
    private val service: NewsService
) {

    fun asLiveData() = liveData {
        val result = newsDao.findNewsSearchResult(category)
        if (result == null) {
            emit(null)
            return@liveData
        }
        val current = result.newsIds.size
        if (current >= result.total) {
            emit(Resource.success(false))
            return@liveData
        }

        try {
            val response = service.searchNewsByCategory(
                category = category,
                date = date,
                count = count,
                page = current / count + 1
            )
            val news = response.article.news
            val bookmarks = newsDao.findBookmarks()
            news.forEach { newData ->
                // We prevent overriding bookmark field
                newData.bookmark = bookmarks.any { currentData ->
                    currentData.id == newData.id
                }
            }

            // We merge all new search result into current result list
            val newsIds = mutableListOf<String>()
            newsIds.addAll(result.newsIds)
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
            emit(Resource.success(true))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", true))
        }
    }

}