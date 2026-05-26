package com.cit.pawnscan.features.auth.api

data class BusinessProfileResponse(
    val businessName: String?,
    val businessAddress: String?,
    val permitNumber: String?,
    val verified: Boolean?,
    val isVerified: Boolean?,
    val rejected: Boolean?,
    val isRejected: Boolean?,
    val rejectionReason: String?
)
