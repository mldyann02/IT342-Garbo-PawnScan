package com.cit.pawnscan.features.dashboard.api

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("notifId") val notifId: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("targetUrl") val targetUrl: String?,
    @SerializedName("read") val read: Boolean,
    @SerializedName("createdAt") val createdAt: String?
)
