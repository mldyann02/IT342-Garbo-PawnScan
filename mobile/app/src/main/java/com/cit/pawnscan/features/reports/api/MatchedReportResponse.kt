package com.cit.pawnscan.features.reports.api

data class MatchedReportResponse(
    val matchId: Long?,
    val reportId: Long?,
    val serialNumber: String?,
    val itemModel: String?,
    val description: String?,
    val status: String?,
    val reportCreatedAt: String?,
    val matchedAt: String?,
    val matchedByBusinessName: String?,
    val matchedByBusinessEmail: String?,
    val matchedByBusinessPhone: String?,
    val matchedByBusinessPermitNumber: String?,
    val matchedByBusinessAddress: String?,
    val matchedByBusinessRegisteredAt: String?,
    val files: List<ReportFileResponse>?
)
