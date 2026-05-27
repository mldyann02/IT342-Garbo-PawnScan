package com.cit.pawnscan.features.auth.api

import com.google.gson.annotations.SerializedName

data class GoogleAuthRequest(
    @SerializedName("token")
    val token: String,

    @SerializedName("role")
    val role: String? = null
)
