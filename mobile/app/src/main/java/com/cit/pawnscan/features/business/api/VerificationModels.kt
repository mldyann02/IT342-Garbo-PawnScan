package com.cit.pawnscan.features.business.api

data class VerifySearchResponse(
    val status: String?,
    val serial: String?,
    val report: StolenReportSummary?,
    val publicApiChecked: Boolean?,
    val publicApiStolen: Boolean?,
    val publicApiSource: String?,
    val publicApiMatchTitle: String?,
    val publicApiMatchUrl: String?
)

data class StolenReportSummary(
    val reportId: Long?,
    val serialNumber: String?,
    val itemModel: String?,
    val description: String?,
    val dateReported: String?,
    val ownerName: String?,
    val victimName: String?,
    val ownerEmail: String?,
    val ownerPhoneNumber: String?,
    val files: List<VerificationFile>?
)

data class SearchLogResponse(
    val searchedSerial: String?,
    val itemModel: String?,
    val result: String?,
    val timestamp: String?,
    val matchedReportId: Long?
)

data class StolenMatchResponse(
    val searchedSerial: String?,
    val timestamp: String?,
    val matchedReportId: Long?,
    val itemModel: String?,
    val description: String?,
    val dateReported: String?,
    val victimName: String?,
    val victimEmail: String?,
    val victimPhoneNumber: String?,
    val files: List<VerificationFile>?
)

data class VerificationFile(
    val id: Long?,
    val fileUrl: String?,
    val fileType: String?
)
