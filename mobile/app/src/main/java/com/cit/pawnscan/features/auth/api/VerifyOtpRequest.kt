package com.cit.pawnscan.features.auth.api

data class VerifyOtpRequest(
    val email: String,
    val code: String
)
