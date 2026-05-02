package com.zarkit.zarkit_user.api


data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val fullAddress: String,
    val gstin: String
)