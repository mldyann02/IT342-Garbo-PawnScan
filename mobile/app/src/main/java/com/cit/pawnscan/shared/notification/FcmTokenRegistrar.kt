package com.cit.pawnscan.shared.notification

import android.content.Context
import android.util.Log
import com.cit.pawnscan.features.dashboard.api.FcmTokenRequest
import com.cit.pawnscan.shared.auth.JwtStorageUtil
import com.cit.pawnscan.shared.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FcmTokenRegistrar {
    private const val TAG = "PawnScanFCM"

    fun registerCurrentToken(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                registerToken(context, token)
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Unable to fetch FCM token", error)
            }
    }

    fun registerToken(context: Context, token: String) {
        val authHeader = authHeader(context) ?: return

        RetrofitClient.getNotificationService()
            .registerFcmToken(authHeader, FcmTokenRequest(token = token, platform = "android"))
            .enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(
                    call: Call<Map<String, Boolean>>,
                    response: Response<Map<String, Boolean>>
                ) {
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Backend rejected FCM token registration: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    Log.w(TAG, "Failed to register FCM token with backend", t)
                }
            })
    }

    fun unregisterCurrentToken(context: Context) {
        val authHeader = authHeader(context) ?: return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                RetrofitClient.getNotificationService()
                    .unregisterFcmToken(authHeader, FcmTokenRequest(token = token, platform = "android"))
                    .enqueue(object : Callback<Map<String, Boolean>> {
                        override fun onResponse(
                            call: Call<Map<String, Boolean>>,
                            response: Response<Map<String, Boolean>>
                        ) {
                            if (!response.isSuccessful) {
                                Log.w(TAG, "Backend rejected FCM token unregister: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                            Log.w(TAG, "Failed to unregister FCM token with backend", t)
                        }
                    })
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Unable to fetch FCM token for unregister", error)
            }
    }

    private fun authHeader(context: Context): String? {
        val token = JwtStorageUtil.getToken(context.applicationContext)?.takeIf { it.isNotBlank() }
        return token?.let { "Bearer $it" }
    }
}
