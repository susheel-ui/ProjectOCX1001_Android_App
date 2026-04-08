package com.zarkit.zarkit_user.api

data class RegisterBody(
    val mobile: String,
    val firstName: String,
    val lastName: String? = null,     // optional
    val email: String? = null,        // optional
    val role: String = "USER"
)
