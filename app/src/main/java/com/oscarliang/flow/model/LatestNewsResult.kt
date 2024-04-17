package com.oscarliang.flow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.oscarliang.flow.db.NewsTypeConverters

@Entity(tableName = "latest_news_results")
@TypeConverters(NewsTypeConverters::class)
data class LatestNewsResult(
    @PrimaryKey
    val query: String,
    val newsIds: List<Int>
)