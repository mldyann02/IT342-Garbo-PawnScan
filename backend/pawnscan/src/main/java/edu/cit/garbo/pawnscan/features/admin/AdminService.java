package edu.cit.garbo.pawnscan.features.admin;

import edu.cit.garbo.pawnscan.features.admin.dto.AdminStatsResponse;
import edu.cit.garbo.pawnscan.features.admin.dto.BusinessProfileAdminResponse;
import edu.cit.garbo.pawnscan.features.admin.dto.ReportAdminResponse;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportStatus;

import java.util.List;

public interface AdminService {
    List<ReportAdminResponse> getPendingReports();
    ReportAdminResponse updateReportStatus(Long reportId, ReportStatus status, String rejectionReason);
    List<BusinessProfileAdminResponse> getPendingBusinesses();
    List<BusinessProfileAdminResponse> getAllBusinesses();
    BusinessProfileAdminResponse verifyBusiness(Long userId);
    BusinessProfileAdminResponse rejectBusiness(Long userId, String rejectionReason);
    AdminStatsResponse getSystemStats();
}
