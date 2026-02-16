package com.example.project_a_android_userapp.api

data class VerifyPaymentRequest(
    val rideId: Long,
    val paymentId: String,
    val orderId: String,
    val signature: String
)