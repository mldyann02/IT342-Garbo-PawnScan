package com.cit.pawnscan.features.auth.api

data class UserProfileResponse(
    val userId: Long?,
    val email: String?,
    val fullName: String?,
    val phoneNumber: String?,
    val role: String?,
    val registrationStatus: String?,
    val createdAt: String?,
    val businessProfile: BusinessProfileResponse?,
    val message: String?
)
