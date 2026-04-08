package com.zarkit.zarkit_user.api

data class RideRequestBody(
    val message: String,
    val fare: Double,
    val vehicle: String,
    val pickup: String,
    val drop: String,
    val distance: String
)
