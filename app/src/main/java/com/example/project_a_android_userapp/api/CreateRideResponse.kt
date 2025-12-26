package com.example.project_a_android_userapp.api

data class CreateRideResponse(
    val rideId: Long,
    val status: String,
    val vehicleInfo: String,
    val finalFare: Double
)
