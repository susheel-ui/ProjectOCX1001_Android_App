package com.example.project_a_android_userapp

import android.app.Application

class MyApp : Application() {

    companion object {
        // Global application context for Retrofit / SharedPreferences
        lateinit var appContext: Application
            private set
    }

    // Global ViewModel instance (your previous code)
    val vm: LocationViewModel by lazy { LocationViewModel() }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}
