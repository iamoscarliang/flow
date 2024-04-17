package com.oscarliang.flow.api

import com.google.gson.annotations.SerializedName
import com.oscarliang.flow.model.News

data class NewsSearchResponse(
    @SerializedName("available")
    val available: Int = 0,
    @SerializedName("news")
    val news: List<News>
)