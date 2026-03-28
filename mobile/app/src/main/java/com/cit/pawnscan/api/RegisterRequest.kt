package com.cit.pawnscan.api

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("fullName")
    val fullName: String? = null,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("businessName")
    val businessName: String? = null,

    @SerializedName("businessAddress")
    val businessAddress: String? = null,

    @SerializedName("permitNumber")
    val permitNumber: String? = null,

    @SerializedName("role")
    val role: String
)
