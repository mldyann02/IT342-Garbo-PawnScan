package com.cit.pawnscan.features.auth.api

data class CompleteProfileRequest(
    val phoneNumber: String,
    val businessName: String? = null,
    val businessAddress: String? = null,
    val permitNumber: String? = null
)
