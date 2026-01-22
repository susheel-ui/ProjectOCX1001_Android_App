package com.example.project_a_android_userapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.libraries.places.api.Places

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

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        // ✅ INITIALIZE GOOGLE PLACES ONCE (FIX)
        if (!Places.isInitialized()) {
            Places.initialize(
                applicationContext,
                getString(R.string.google_maps_key)
            )
        }
    }
}
