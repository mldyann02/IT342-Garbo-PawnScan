package com.cit.pawnscan.features.auth.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthService {
    @POST("/api/auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("/api/auth/verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<AuthResponse>

    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @GET("/api/auth/google/config")
    fun getGoogleAuthConfig(): Call<GoogleAuthConfigResponse>

    @POST("/api/auth/google")
    fun authenticateWithGoogle(@Body request: GoogleAuthRequest): Call<AuthResponse>

    @PUT("/api/auth/complete-profile")
    fun completeProfile(
        @Header("Authorization") authorization: String,
        @Body request: CompleteProfileRequest
    ): Call<AuthResponse>

    @GET("/api/auth/profile")
    fun getProfile(@Header("Authorization") authorization: String): Call<UserProfileResponse>

    @PUT("/api/auth/profile")
    fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UserProfileUpdateRequest
    ): Call<UserProfileResponse>

    @POST("/api/auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<MessageResponse>

    @POST("/api/auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<MessageResponse>
}


