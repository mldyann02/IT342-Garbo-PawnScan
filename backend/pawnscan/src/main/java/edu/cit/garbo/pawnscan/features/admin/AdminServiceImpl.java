package edu.cit.garbo.pawnscan.features.admin;

import edu.cit.garbo.pawnscan.features.admin.dto.AdminStatsResponse;
import edu.cit.garbo.pawnscan.features.admin.dto.BusinessProfileAdminResponse;
import edu.cit.garbo.pawnscan.features.admin.dto.ReportAdminResponse;
import edu.cit.garbo.pawnscan.features.businessprofile.entity.BusinessProfile;
import edu.cit.garbo.pawnscan.features.businessprofile.repository.BusinessProfileRepository;
import edu.cit.garbo.pawnscan.features.notifications.NotificationService;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportFileResponse;
import edu.cit.garbo.pawnscan.features.reports.entity.Report;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportFile;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportStatus;
import edu.cit.garbo.pawnscan.features.reports.exception.ReportNotFoundException;
import edu.cit.garbo.pawnscan.features.reports.repository.ReportRepository;
import edu.cit.garbo.pawnscan.shared.user.User;
import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ReportRepository reportRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<ReportAdminResponse> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING)
                .stream()
                .map(this::toReportAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReportAdminResponse updateReportStatus(Long reportId, ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found"));
        
        report.setStatus(status);
        Report savedReport = reportRepository.save(report);
        notifyReportOwner(savedReport);
        
        return toReportAdminResponse(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessProfileAdminResponse> getPendingBusinesses() {
        return businessProfileRepository.findByIsVerified(false)
                .stream()
                .map(this::toBusinessProfileAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessProfileAdminResponse> getAllBusinesses() {
        return businessProfileRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BusinessProfile::getCreatedAt).reversed())
                .map(this::toBusinessProfileAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BusinessProfileAdminResponse verifyBusiness(Long userId) {
        BusinessProfile profile = businessProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Business Profile not found"));
        
        profile.setIsVerified(true);
        BusinessProfile savedProfile = businessProfileRepository.save(profile);
        notifyBusinessOwner(savedProfile);
        
        return toBusinessProfileAdminResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getSystemStats() {
        long totalUsers = userRepository.count();
        long totalBusinesses = businessProfileRepository.count();
        
        // In-memory filter or count query. A custom count query would be better, but this works for now.
        long pendingReports = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING).size();
        long pendingBusinesses = businessProfileRepository.findByIsVerified(false).size();

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalBusinesses(totalBusinesses)
                .pendingReports(pendingReports)
                .pendingBusinesses(pendingBusinesses)
                .build();
    }

    private ReportAdminResponse toReportAdminResponse(Report report) {
        List<ReportFileResponse> files = report.getFiles().stream()
                .sorted(Comparator.comparing(ReportFile::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(file -> ReportFileResponse.builder()
                        .id(file.getId())
                        .fileUrl(file.getFileUrl())
                        .fileType(file.getFileType())
                        .build())
                .collect(Collectors.toList());

        User user = report.getUser();
        
        return ReportAdminResponse.builder()
                .id(report.getId())
                .serialNumber(report.getSerialNumber())
                .itemModel(report.getItemModel())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .ownerName(user != null ? user.getFullName() : null)
                .ownerEmail(user != null ? user.getEmail() : null)
                .files(files)
                .build();
    }

    private void notifyReportOwner(Report report) {
        User owner = report.getUser();
        if (owner == null) {
            return;
        }

        String itemModel = report.getItemModel() == null || report.getItemModel().isBlank()
                ? "Your report"
                : report.getItemModel();
        String status = report.getStatus() == null ? "updated" : report.getStatus().name().toLowerCase();

        notificationService.createNotification(
                owner,
                "Report status updated",
                itemModel + " has been marked " + status + ".",
                "/reports?status=" + report.getStatus().name() + "&reportId=" + report.getId());
    }

    private void notifyBusinessOwner(BusinessProfile profile) {
        User owner = profile.getUser();
        if (owner == null) {
            return;
        }

        String businessName = profile.getBusinessName() == null || profile.getBusinessName().isBlank()
                ? "Your business account"
                : profile.getBusinessName();

        notificationService.createNotification(
                owner,
                "Business account approved",
                businessName + " has been approved by an administrator.",
                "/business");
    }

    private BusinessProfileAdminResponse toBusinessProfileAdminResponse(BusinessProfile profile) {
        User user = profile.getUser();
        
        return BusinessProfileAdminResponse.builder()
                .userId(profile.getUserId())
                .businessName(profile.getBusinessName())
                .businessAddress(profile.getBusinessAddress())
                .permitNumber(profile.getPermitNumber())
                .isVerified(profile.getIsVerified())
                .ownerName(user != null ? user.getFullName() : null)
                .ownerEmail(user != null ? user.getEmail() : null)
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
