package com.example.project_a_android_userapp.model

data class BookAgainResponse(
    val pickupLat: Double,
    val pickupLon: Double,
    val pickupAddress: String,

    val dropLat: Double,
    val dropLon: Double,
    val dropAddress: String,

    val distanceText: String,
    val durationText: String,
    val distanceValue: Int,
    val durationValue: Int,

    val senderHouse: String,
    val senderName: String,
    val senderPhone: String,
    val senderType: String,

    val receiverHouse: String,
    val receiverName: String,
    val receiverPhone: String,
    val receiverType: String
)
