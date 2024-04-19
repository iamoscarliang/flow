package com.oscarliang.flow.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oscarliang.flow.model.Category
import com.oscarliang.flow.model.News
import com.oscarliang.flow.model.NewsSearchResult

@Database(
    entities = [News::class, NewsSearchResult::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao

    abstract fun categoryDao(): CategoryDao

}