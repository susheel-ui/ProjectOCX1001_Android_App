package com.example.project_a_android_userapp.api

data class RideNotificationRequest(
    val rideId: Long,
    val message: String,
    val fare: Double,
    val vehicle: String,
    val pickup: String,
    val drop: String,
    val distance: String
)
