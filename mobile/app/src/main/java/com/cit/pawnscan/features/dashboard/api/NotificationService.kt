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
}
