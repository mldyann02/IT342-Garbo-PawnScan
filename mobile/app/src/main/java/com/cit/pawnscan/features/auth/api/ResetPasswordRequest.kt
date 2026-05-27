package com.cit.pawnscan.features.auth.api

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
