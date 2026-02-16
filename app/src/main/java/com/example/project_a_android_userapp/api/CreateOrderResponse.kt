package com.example.project_a_android_userapp.api

data class CreateOrderResponse(
    val orderId: String,
    val amount: Int,
    val currency: String
)