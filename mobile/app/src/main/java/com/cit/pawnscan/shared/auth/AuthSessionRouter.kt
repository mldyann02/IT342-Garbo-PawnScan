package com.cit.pawnscan.shared.auth

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.cit.pawnscan.features.auth.LoginActivity
import com.cit.pawnscan.features.auth.CompleteProfileActivity
import com.cit.pawnscan.features.auth.api.UserProfileResponse
import com.cit.pawnscan.features.business.BusinessDashboardActivity
import com.cit.pawnscan.features.dashboard.UserDashboardActivity
import com.cit.pawnscan.features.landing.MainActivity
import com.cit.pawnscan.shared.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object AuthSessionRouter {
    private const val ADMIN_ROLE = "ADMIN"
    private const val BUSINESS_ROLE = "BUSINESS"

    fun routeFromSplash(activity: Activity, minimumSplashMillis: Long = 2000L) {
        val startedAt = SystemClock.elapsedRealtime()
        val token = JwtStorageUtil.getToken(activity)

        if (token.isNullOrBlank()) {
            runAfterMinimumSplash(activity, startedAt, minimumSplashMillis) {
                launchLanding(activity)
            }
            return
        }

        RetrofitClient.getAuthService()
            .getProfile("Bearer $token")
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    runAfterMinimumSplash(activity, startedAt, minimumSplashMillis) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                val profile = response.body()!!
                                saveProfileSnapshot(activity, profile)
                                if (profile.registrationStatus == "INCOMPLETE") {
                                    launchAndFinish(activity, CompleteProfileActivity::class.java)
                                } else {
                                    routeAfterAuthentication(activity)
                                }
                            }
                            response.code() == 401 || response.code() == 403 -> {
                                JwtStorageUtil.clearAll(activity)
                                launchLanding(activity)
                            }
                            else -> routeAfterAuthentication(activity)
                        }
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    runAfterMinimumSplash(activity, startedAt, minimumSplashMillis) {
                        routeAfterAuthentication(activity)
                    }
                }
            })
    }

    fun routeAfterAuthentication(activity: Activity) {
        val role = JwtStorageUtil.getUserRole(activity)

        if (role == ADMIN_ROLE) {
            JwtStorageUtil.clearAll(activity)
            launchLogin(activity)
            return
        }

        launchAuthenticatedPortal(activity)
    }

    private fun saveProfileSnapshot(activity: Activity, profile: UserProfileResponse) {
        profile.email?.takeIf { it.isNotBlank() }?.let {
            JwtStorageUtil.saveUserEmail(activity, it)
        }
        profile.role?.takeIf { it.isNotBlank() }?.let {
            JwtStorageUtil.saveUserRole(activity, it)
        }
    }

    private fun runAfterMinimumSplash(
        activity: Activity,
        startedAt: Long,
        minimumSplashMillis: Long,
        action: () -> Unit
    ) {
        val elapsed = SystemClock.elapsedRealtime() - startedAt
        val remaining = (minimumSplashMillis - elapsed).coerceAtLeast(0L)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!activity.isFinishing && !activity.isDestroyed) {
                action()
            }
        }, remaining)
    }

    private fun launchLanding(activity: Activity) {
        launchAndFinish(activity, MainActivity::class.java)
    }

    private fun launchLogin(activity: Activity) {
        launchAndFinish(activity, LoginActivity::class.java)
    }

    private fun launchAuthenticatedPortal(activity: Activity) {
        val target = if (JwtStorageUtil.getUserRole(activity) == BUSINESS_ROLE) {
            BusinessDashboardActivity::class.java
        } else {
            UserDashboardActivity::class.java
        }
        launchAndFinish(activity, target)
    }

    private fun launchAndFinish(activity: Activity, target: Class<out Activity>) {
        val intent = Intent(activity, target)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}
