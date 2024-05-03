package com.oscarliang.flow.di

import android.content.Context
import androidx.room.Room
import com.oscarliang.flow.MainViewModel
import com.oscarliang.flow.api.NewsService
import com.oscarliang.flow.db.NewsDatabase
import com.oscarliang.flow.repository.CategoryRepository
import com.oscarliang.flow.repository.NewsRepository
import com.oscarliang.flow.repository.UserRepository
import com.oscarliang.flow.ui.bookmarks.BookmarksViewModel
import com.oscarliang.flow.ui.news.NewsViewModel
import com.oscarliang.flow.ui.newsdetail.NewsDetailViewModel
import com.oscarliang.flow.ui.search.SearchViewModel
import com.oscarliang.flow.ui.settings.SettingsViewModel
import com.oscarliang.flow.util.DB_NAME
import com.oscarliang.flow.util.NEWS_URL
import com.oscarliang.flow.util.PREFERENCE_NAME
import com.oscarliang.flow.util.REFRESH_TIMEOUT
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
            .baseUrl(NEWS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsService::class.java)
    }

    single {
        Room.databaseBuilder(androidContext(), NewsDatabase::class.java, DB_NAME)
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
        androidContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    single {
        RateLimiter<String>(REFRESH_TIMEOUT, TimeUnit.MINUTES)
    }

    single {
        NewsRepository(get(), get(), get(), get())
    }

    single {
        CategoryRepository(get(), get())
    }

    single {
        UserRepository(get())
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

    viewModel {
        NewsDetailViewModel(get())
    }

    viewModel {
        SettingsViewModel(get())
    }

    viewModel {
        MainViewModel(get())
    }

}