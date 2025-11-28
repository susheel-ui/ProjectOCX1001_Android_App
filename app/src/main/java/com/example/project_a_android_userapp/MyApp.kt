package com.example.project_a_android_userapp

import android.app.Application

class MyApp : Application() {
    // single store instance for the whole app process
    val vm: LocationViewModel by lazy { LocationViewModel() }
}
