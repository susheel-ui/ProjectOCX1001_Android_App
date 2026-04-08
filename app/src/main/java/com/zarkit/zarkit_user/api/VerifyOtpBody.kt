package com.zarkit.zarkit_user.api

data class VerifyOtpBody(
    val mobile: String,
    val otp: String
)
