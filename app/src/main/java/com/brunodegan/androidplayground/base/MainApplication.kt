package com.brunodegan.androidplayground.base

import android.app.Application
import android.graphics.Bitmap
import coil.Coil
import coil.ImageLoader
import com.brunodegan.androidplayground.data.sync.MovieSyncScheduler
import com.brunodegan.androidplayground.di.AppModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.plugin.module.dsl.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader
                .Builder(this)
                .crossfade(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build(),
        )

        startKoin<AppModule> {
            androidLogger()
            androidContext(this@MainApplication)
            workManagerFactory()
        }

        get<MovieSyncScheduler>().schedule()
    }
}
