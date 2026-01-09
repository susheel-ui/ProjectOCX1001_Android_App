package com.example.project_a_android_userapp.api

import android.util.Log
import com.example.project_a_android_userapp.LocalStorage
import com.example.project_a_android_userapp.MyApp
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
 
    private const val BASE_URL = "http://192.168.29.149:8080/"

    // Add JWT token to header
    private val authInterceptor = Interceptor { chain ->

        val token = LocalStorage.getToken(MyApp.appContext)

        Log.d("API_AUTH", "JWT Token Used: $token")

        val newRequest = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        chain.proceed(newRequest)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
