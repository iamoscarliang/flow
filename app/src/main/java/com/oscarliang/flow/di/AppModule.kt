package com.oscarliang.flow.di

import androidx.room.Room
import com.oscarliang.flow.api.NewsService
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.repository.CategoryRepository
import com.oscarliang.flow.repository.NewsRepository
import com.oscarliang.flow.ui.bookmarks.BookmarksViewModel
import com.oscarliang.flow.ui.news.NewsViewModel
import com.oscarliang.flow.ui.search.SearchViewModel
import com.oscarliang.flow.util.RateLimiter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single {
        Retrofit.Builder()
            .baseUrl("https://eventregistry.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsService::class.java)
    }

    single {
        Room.databaseBuilder(androidContext(), NewsDatabase::class.java, "news.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        val db = get<NewsDatabase>()
        db.newsDao()
    }

    single {
        val db = get<NewsDatabase>()
        db.categoryDao()
    }

    single {
        RateLimiter<String>(10, TimeUnit.MINUTES)
    }

    single {
        NewsRepository(get(), get(), get(), get())
    }

    single {
        CategoryRepository(get(), get())
    }

    viewModel {
        NewsViewModel(get(), get())
    }

    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        BookmarksViewModel(get())
    }

}