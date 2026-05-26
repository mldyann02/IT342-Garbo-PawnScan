package com.cit.pawnscan.features.auth.api

data class UserProfileUpdateRequest(
    val fullName: String,
    val phoneNumber: String,
    val businessName: String? = null,
    val businessAddress: String? = null,
    val permitNumber: String? = null
)
