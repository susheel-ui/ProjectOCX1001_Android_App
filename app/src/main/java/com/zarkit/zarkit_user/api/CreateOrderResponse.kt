package com.zarkit.zarkit_user.api

data class CreateOrderResponse(
    val orderId: String,
    val amount: Int,
    val currency: String
)