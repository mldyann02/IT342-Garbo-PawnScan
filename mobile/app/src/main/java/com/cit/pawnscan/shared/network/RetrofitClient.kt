package com.cit.pawnscan.shared.network

import com.cit.pawnscan.BuildConfig
import com.cit.pawnscan.features.auth.api.AuthService
import com.cit.pawnscan.features.business.api.VerificationService
import com.cit.pawnscan.features.reports.api.ReportService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null

    fun getInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(normalizedBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getAuthService(): AuthService {
        return getInstance().create(AuthService::class.java)
    }

    fun getReportService(): ReportService {
        return getInstance().create(ReportService::class.java)
    }

    fun getVerificationService(): VerificationService {
        return getInstance().create(VerificationService::class.java)
    }

    private fun normalizedBaseUrl(): String {
        val configuredUrl = BuildConfig.PAWNSCAN_API_BASE_URL.trim()
        return if (configuredUrl.endsWith("/")) configuredUrl else "$configuredUrl/"
    }
}


