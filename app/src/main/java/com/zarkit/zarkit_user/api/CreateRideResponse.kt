package com.zarkit.zarkit_user.api

data class CreateRideResponse(
    val rideId: Long,
    val status: String,
    val vehicleInfo: String,
    val finalFare: Double
)
