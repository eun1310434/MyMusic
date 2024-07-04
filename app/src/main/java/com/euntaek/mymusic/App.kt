package com.euntaek.mymusic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
    companion object {
        lateinit var appId:String
    }
    override fun onCreate() {
        super.onCreate()
        appId = applicationContext.getString(R.string.app_id)
        Timber.plant(Timber.DebugTree())
    }
}