package com.oscarliang.flow.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "news")
data class News(
    @PrimaryKey
    @SerializedName("uri")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("image")
    val image: String?,
    @Embedded(prefix = "source_")
    @SerializedName("source")
    val source: Source?,
    var bookmark: Boolean = false
) {

    data class Source(
        @SerializedName("uri")
        val id: String,
        @SerializedName("title")
        val title: String
    )

}