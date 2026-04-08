package com.zarkit.zarkit_user.model

data class Booking(
    val rideId: Long,
    val finalFare: Double,
    val pickupAddress: String,
    val dropAddress: String,
    val distanceText: String?,
    val createdAt: String,
    val vehicleInfo: String,
    val status: String
)
