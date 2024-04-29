package com.oscarliang.flow.api

import com.oscarliang.flow.util.API_KEY
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {

    @GET("api/v1/article/getArticles")
    suspend fun getLatestNews(
        @Query("apiKey") key: String = API_KEY,
        @Query("lang") language: String = "eng",
        @Query("dateStart") date: String,
        @Query("articlesCount") count: Int
    ): NewsSearchResponse

    @GET("api/v1/article/getArticles")
    suspend fun getNewsBySource(
        @Query("apiKey") key: String = API_KEY,
        @Query("lang") language: String = "eng",
        @Query("sourceUri") source: String,
        @Query("articlesCount") count: Int
    ): NewsSearchResponse

    @GET("api/v1/article/getArticles")
    suspend fun getNewsByCategory(
        @Query("apiKey") key: String = API_KEY,
        @Query("lang") language: String = "eng",
        @Query("categoryUri") category: String,
        @Query("dateStart") date: String,
        @Query("articlesCount") count: Int,
        @Query("articlesPage") page: Int = 1
    ): NewsSearchResponse

    @GET("api/v1/article/getArticles")
    suspend fun searchNews(
        @Query("apiKey") key: String = API_KEY,
        @Query("lang") language: String = "eng",
        @Query("forceMaxDataTimeWindow") maxDataTime: Int = 7,
        @Query("keyword") keyword: String,
        @Query("articlesCount") count: Int,
        @Query("articlesPage") page: Int = 1
    ): NewsSearchResponse

}