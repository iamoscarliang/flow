package com.oscarliang.flow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "news")
data class News(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("image")
    val image: String?,
    @SerializedName("publish_date")
    val publishDate: String?,
    @SerializedName("author")
    val author: String?,
    var bookmark: Boolean = false
)