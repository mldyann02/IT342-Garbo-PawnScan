package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.ReportResponse;
import edu.cit.garbo.pawnscan.dto.ReportUpsertRequest;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(String authenticatedEmail, ReportUpsertRequest request);

    List<ReportResponse> getUserReports(String authenticatedEmail);

    ReportResponse updateReport(String authenticatedEmail, Long reportId, ReportUpsertRequest request);

    void deleteReport(String authenticatedEmail, Long reportId);
}