package com.oscarliang.flow.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oscarliang.flow.model.LatestNewsResult
import com.oscarliang.flow.model.News
import com.oscarliang.flow.model.NewsSearchResult

@Database(
    entities = [News::class, LatestNewsResult::class, NewsSearchResult::class],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao

}