package com.example.project_a_android_userapp.api

data class DriverContactResponse(
    val driverId: Long,
    val driverName: String,
    val driverPhone: String,
    val vehicleType: String,
    val vehicleNumber: String,
    val rideStatus: String?,
    val driverPhotoUrl: String?,
    val finalFare: Double,
    val isPaymentDone: Boolean
)
