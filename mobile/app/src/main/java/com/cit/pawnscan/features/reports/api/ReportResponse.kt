package com.cit.pawnscan.features.reports.api

data class ReportResponse(
    val id: Long?,
    val serialNumber: String?,
    val itemModel: String?,
    val description: String?,
    val status: String?,
    val rejectionReason: String?,
    val createdAt: String?,
    val files: List<ReportFileResponse>?
)
