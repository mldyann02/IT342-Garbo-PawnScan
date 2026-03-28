package com.cit.pawnscan.api

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("userId")
    val userId: Long? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("fullName")
    val fullName: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("token")
    val token: String? = null,

    @SerializedName("businessProfile")
    val businessProfile: BusinessProfileSummary? = null,

    @SerializedName("message")
    val message: String? = null
)

data class BusinessProfileSummary(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("businessName")
    val businessName: String? = null,

    @SerializedName("businessAddress")
    val businessAddress: String? = null
)
