package com.cit.pawnscan.features.dashboard.api

import retrofit2.Call
import retrofit2.http.*

interface NotificationService {
    @GET("api/notifications")
    fun getNotifications(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Call<List<NotificationResponse>>

    @GET("api/notifications/unread-count")
    fun getUnreadCount(
        @Header("Authorization") token: String
    ): Call<UnreadCountResponse>

    @PATCH("api/notifications/{id}/read")
    fun markAsRead(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<NotificationResponse>

    @POST("api/notifications/read-all")
    fun markAllAsRead(
        @Header("Authorization") token: String
    ): Call<Map<String, Boolean>>

    @DELETE("api/notifications")
    fun clearNotifications(
        @Header("Authorization") token: String
    ): Call<Map<String, Boolean>>

    @POST("api/notifications/fcm-tokens")
    fun registerFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequest
    ): Call<Map<String, Boolean>>

    @POST("api/notifications/fcm-tokens/unregister")
    fun unregisterFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequest
    ): Call<Map<String, Boolean>>
}
