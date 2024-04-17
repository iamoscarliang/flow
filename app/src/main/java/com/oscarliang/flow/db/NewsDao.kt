package com.oscarliang.flow.db

import android.util.SparseIntArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oscarliang.flow.model.LatestNewsResult
import com.oscarliang.flow.model.News
import com.oscarliang.flow.model.NewsSearchResult

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLatestNewsResult(latestNewsResult: LatestNewsResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsSearchResult(newsSearchResult: NewsSearchResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: List<News>)

    @Query("SELECT * FROM news_search_results WHERE `query` = :query")
    suspend fun findLatestNewsResult(query: String): LatestNewsResult?

    @Query("SELECT * FROM news_search_results WHERE `query` = :query")
    suspend fun findNewsSearchResult(query: String): NewsSearchResult?

    @Query("SELECT * FROM news WHERE id in (:newsIds)")
    suspend fun findNewsById(newsIds: List<Int>): List<News>

    @Query("SELECT * FROM news WHERE bookmark = 1")
    suspend fun findBookmarks(): List<News>

    @Query("SELECT * FROM news_search_results WHERE `query` = :query")
    fun getLatestNewsResult(query: String): LiveData<LatestNewsResult?>

    @Query("SELECT * FROM news_search_results WHERE `query` = :query")
    fun getNewsSearchResult(query: String): LiveData<NewsSearchResult?>

    fun getNewsByOrder(newsIds: List<Int>): LiveData<List<News>> {
        val order = SparseIntArray()
        newsIds.withIndex().forEach {
            order.put(it.value, it.index)
        }
        return getNewsById(newsIds).map { news ->
            news.sortedWith(compareBy { order.get(it.id) })
        }
    }

    @Query("SELECT * FROM news WHERE id in (:newsIds)")
    fun getNewsById(newsIds: List<Int>): LiveData<List<News>>

    @Query("SELECT * FROM news WHERE id = :id")
    fun getNewsById(id: Int): LiveData<News>

    @Query("SELECT * FROM news WHERE bookmark = 1")
    fun getBookmarks(): LiveData<List<News>>

    @Update
    suspend fun updateNews(news: News)

}