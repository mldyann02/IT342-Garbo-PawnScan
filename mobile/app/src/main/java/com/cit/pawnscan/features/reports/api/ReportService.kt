package com.cit.pawnscan.features.reports.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportService {
    @GET("/api/reports")
    fun getReports(@Header("Authorization") authorization: String): Call<List<ReportResponse>>

    @GET("/api/reports/matched")
    fun getMatchedReports(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Call<List<MatchedReportResponse>>

    @Multipart
    @POST("/api/reports")
    fun createReport(
        @Header("Authorization") authorization: String,
        @Part("serialNumber") serialNumber: RequestBody,
        @Part("itemModel") itemModel: RequestBody,
        @Part("description") description: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Call<ReportResponse>

    @Multipart
    @PUT("/api/reports/{id}")
    fun updateReport(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long,
        @Part("serialNumber") serialNumber: RequestBody,
        @Part("itemModel") itemModel: RequestBody,
        @Part("description") description: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Call<ReportResponse>

    @DELETE("/api/reports/{id}")
    fun deleteReport(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Call<Void>
}
