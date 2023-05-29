package com.example.geolegacy
import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapDatabaseManager.initialize(this)
    }
}
