package com.cit.pawnscan.features.auth.api

import com.google.gson.annotations.SerializedName

data class GoogleAuthConfigResponse(
    @SerializedName("configured")
    val configured: Boolean = false,

    @SerializedName("clientId")
    val clientId: String? = null
)
