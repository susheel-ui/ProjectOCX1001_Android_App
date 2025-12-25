package com.example.project_a_android_userapp.api

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/send-ride-notification")
    fun sendRideRequest(@Body body: RideRequestBody): Call<ResponseBody>

    @POST("/api/driver/save-fcm")
    fun saveDriverToken(@Body body: SaveTokenBody): Call<ResponseBody>

    @POST("/auth/verify")
    fun verifyOtp(@Body body: VerifyOtpBody): Call<ResponseBody>

    @POST("user/calculate")
    fun calculateFare(
        @Body body: JsonObject
    ): Call<List<JsonObject>>


}
