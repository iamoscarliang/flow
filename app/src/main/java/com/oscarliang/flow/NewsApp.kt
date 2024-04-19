package com.oscarliang.flow

import android.app.Application
import com.oscarliang.flow.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NewsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NewsApp)
            modules(appModule)
        }
    }

}