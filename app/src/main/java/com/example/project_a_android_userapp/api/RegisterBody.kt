package com.example.project_a_android_userapp.api

data class RegisterBody(
    val mobile: String,
    val firstName: String,
    val lastName: String? = null,     // optional
    val email: String? = null,        // optional
    val role: String = "USER"
)
