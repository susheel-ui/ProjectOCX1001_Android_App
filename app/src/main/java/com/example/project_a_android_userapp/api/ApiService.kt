package com.example.project_a_android_userapp.api

import com.example.project_a_android_userapp.model.BookAgainResponse
import com.example.project_a_android_userapp.model.Booking
import com.example.project_a_android_userapp.model.UserResponse
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("/user/create-ride")
    fun createRide(
        @Body body: CreateRideRequest
    ): Call<CreateRideResponse>

    @POST("/api/send-ride-notification")
    fun sendRideNotification(
        @Body body: RideNotificationRequest
    ): Call<String>

    @GET("user/driver-contact")
    fun getDriverContact(
        @Query("rideId") rideId: Long
    ): Call<DriverContactResponse>

    @GET("user/ride/status/{rideId}")
    fun getRideStatus(
        @Path("rideId") rideId: Long
    ): Call<RideStatusResponse>

    @GET("images/{fileName}")
    fun getDriverImage(
        @Path("fileName") fileName: String
    ): Call<ResponseBody>

    @GET("user/driver-location")
    fun getDriverLiveLocation(
        @Query("driverId") driverId: Long
    ): Call<DriverLiveLocationResponse>

    @POST("/api/call/ride/connect")
    fun callDriver(
        @Header("Authorization") auth: String,
        @Body body: Map<String, Long>
    ): Call<String>

    @POST("user/{rideId}/cancel")
    fun cancelRide(
        @Path("rideId") rideId: Long,
        @Query("userId") userId: Long,
        @Header("Authorization") authHeader: String
    ): Call<Void>

    @POST("auth/register")
    fun register(
        @Body body: RegisterBody
    ): Call<ResponseBody>

    @GET("user/{id}")
    fun getUserById(
        @Path("id") userId: Int
    ): Call<UserResponse>

    @GET("user/all-booking/{userId}")
    suspend fun getAllBookings(
        @Path("userId") userId: Int
    ): Response<List<Booking>>

    @GET("user/book-again/{rideId}")
    suspend fun bookAgain(
        @Header("Authorization") token: String,
        @Path("rideId") rideId: Long
    ): Response<BookAgainResponse>

    @POST("user/create-order/{rideId}")
    fun createOrder(
        @Path("rideId") rideId: Long,
        @Header("Authorization") token: String
    ): Call<CreateOrderResponse>

    @POST("user/verify")
    fun verifyPayment(
        @Header("Authorization") token: String,
        @Body request: VerifyPaymentRequest
    ): Call<ResponseBody>

}
