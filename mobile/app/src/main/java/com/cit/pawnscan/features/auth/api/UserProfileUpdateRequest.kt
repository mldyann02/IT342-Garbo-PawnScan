package com.cit.pawnscan.features.auth.api

data class UserProfileUpdateRequest(
    val fullName: String,
    val phoneNumber: String
)
