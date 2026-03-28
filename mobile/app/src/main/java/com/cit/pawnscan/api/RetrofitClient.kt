package com.cit.pawnscan.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Update this URL based on your server configuration
    // For Android emulator testing: http://10.0.2.2:8080
    // For physical device: use your server IP address
    private const val BASE_URL = "http://10.0.2.2:8080"

    private var retrofit: Retrofit? = null

    fun getInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getAuthService(): AuthService {
        return getInstance().create(AuthService::class.java)
    }
}
