package com.oscarliang.flow.api

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

private const val API_KEY = "4665ddad2c5941a39c4d0005331c7238"

interface NewsService {

    @Headers("x-api-key: $API_KEY")
    @GET("search-news")
    fun getLatestNews(
        @Query("earliest-publish-date") date: String,
        @Query("language") language: String,
        @Query("number") number: Int
    ): NewsSearchResponse

    @Headers("x-api-key: $API_KEY")
    @GET("search-news")
    fun searchNews(
        @Query("text") query: String,
        @Query("earliest-publish-date") date: String,
        @Query("language") language: String,
        @Query("number") number: Int
    ): NewsSearchResponse

    @Headers("x-api-key: $API_KEY")
    @GET("search-news")
    fun searchNews(
        @Query("text") query: String,
        @Query("earliest-publish-date") date: String,
        @Query("language") language: String,
        @Query("number") number: Int,
        @Query("offset") offset: Int
    ): NewsSearchResponse

}