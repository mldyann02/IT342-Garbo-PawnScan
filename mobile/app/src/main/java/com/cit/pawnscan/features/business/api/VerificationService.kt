package com.cit.pawnscan.features.business.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface VerificationService {
    @GET("/api/verify/search")
    fun verifySerial(
        @Header("Authorization") authorization: String,
        @Query("serial") serial: String
    ): Call<VerifySearchResponse>

    @GET("/api/verify/history")
    fun getSearchHistory(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Call<List<SearchLogResponse>>

    @GET("/api/verify/matches")
    fun getStolenMatches(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Call<List<StolenMatchResponse>>
}
