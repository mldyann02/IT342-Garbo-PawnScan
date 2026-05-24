package edu.cit.garbo.pawnscan.features.reports;

import edu.cit.garbo.pawnscan.features.reports.dto.ReportResponse;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportUpsertRequest;
import edu.cit.garbo.pawnscan.features.reports.dto.MatchedReportResponse;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(String authenticatedEmail, ReportUpsertRequest request);

    List<ReportResponse> getUserReports(String authenticatedEmail);

    List<MatchedReportResponse> getMatchedReports(String authenticatedEmail, int page, int size);

    ReportResponse updateReport(String authenticatedEmail, Long reportId, ReportUpsertRequest request);

    void deleteReport(String authenticatedEmail, Long reportId);
}







