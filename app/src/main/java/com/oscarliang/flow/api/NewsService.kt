package com.oscarliang.flow.api

import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = "c5516eeb-b7ce-45c5-b21b-5588a8d670f4"

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
    suspend fun searchCategory(
        @Query("apiKey") key: String = API_KEY,
        @Query("lang") language: String = "eng",
        @Query("categoryUri") category: String,
        @Query("dateStart") date: String,
        @Query("articlesCount") count: Int
    ): NewsSearchResponse

    @GET("api/v1/article/getArticles")
    suspend fun searchCategory(
        @Query("apiKey") key: String = API_KEY,
        @Query("lang") language: String = "eng",
        @Query("categoryUri") category: String,
        @Query("dateStart") date: String,
        @Query("articlesCount") count: Int,
        @Query("articlesPage") page: Int
    ): NewsSearchResponse

    @GET("api/v1/article/getArticles")
    suspend fun searchNews(
        @Query("apiKey") key: String = API_KEY,
        @Query("lang") language: String = "eng",
        @Query("forceMaxDataTimeWindow") maxDataTime: Int = 7,
        @Query("keyword") keyword: String,
        @Query("articlesCount") count: Int
    ): NewsSearchResponse

    @GET("api/v1/article/getArticles")
    suspend fun searchNews(
        @Query("apiKey") key: String = API_KEY,
        @Query("lang") language: String = "eng",
        @Query("forceMaxDataTimeWindow") maxDataTime: Int = 7,
        @Query("keyword") keyword: String,
        @Query("articlesCount") count: Int,
        @Query("articlesPage") page: Int
    ): NewsSearchResponse

}