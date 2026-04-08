package com.zarkit.zarkit_user.api

data class VerifyPaymentRequest(
    val rideId: Long,
    val paymentId: String,
    val orderId: String,
    val signature: String
)