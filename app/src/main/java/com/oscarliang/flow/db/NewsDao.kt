package com.oscarliang.flow.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oscarliang.flow.model.News
import com.oscarliang.flow.model.NewsSearchResult

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsSearchResult(newsSearchResult: NewsSearchResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<News>)

    @Query("SELECT * FROM news_search_results WHERE `query` = :query")
    suspend fun findNewsSearchResult(query: String): NewsSearchResult?

    @Query("SELECT * FROM news WHERE id in (:newsIds)")
    suspend fun findNewsById(newsIds: List<String>): List<News>

    @Query("SELECT * FROM news WHERE bookmark = 1")
    suspend fun findBookmarks(): List<News>

    @Query("SELECT * FROM news_search_results WHERE `query` = :query")
    fun getNewsSearchResult(query: String): LiveData<NewsSearchResult?>

    fun getNewsByOrder(newsIds: List<String>): LiveData<List<News>> {
        val order = newsIds.withIndex().associate { (index, id) -> id to index }
        return getNewsById(newsIds).map { news ->
            news.sortedBy { order[it.id] }
        }
    }

    @Query("SELECT * FROM news WHERE id in (:newsIds)")
    fun getNewsById(newsIds: List<String>): LiveData<List<News>>

    @Query("SELECT * FROM news WHERE id = :id")
    fun getNewsById(id: String): LiveData<News>

    @Query("SELECT * FROM news WHERE bookmark = 1")
    fun getBookmarks(): LiveData<List<News>>

    @Update
    suspend fun updateNews(news: News)

}