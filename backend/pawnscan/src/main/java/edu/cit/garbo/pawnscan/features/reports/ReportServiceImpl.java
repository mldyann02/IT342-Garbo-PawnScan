package edu.cit.garbo.pawnscan.features.reports;

import edu.cit.garbo.pawnscan.features.reports.dto.MatchedReportResponse;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportFileResponse;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportResponse;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportUpsertRequest;
import edu.cit.garbo.pawnscan.features.businessprofile.entity.BusinessProfile;
import edu.cit.garbo.pawnscan.features.reports.entity.Report;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportFile;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportFileType;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportStatus;
import edu.cit.garbo.pawnscan.shared.user.User;
import edu.cit.garbo.pawnscan.features.reports.exception.DuplicateSerialNumberException;
import edu.cit.garbo.pawnscan.shared.exception.ForbiddenActionException;
import edu.cit.garbo.pawnscan.features.reports.exception.InvalidReportException;
import edu.cit.garbo.pawnscan.features.reports.exception.ReportNotFoundException;
import edu.cit.garbo.pawnscan.features.reports.repository.ReportFileRepository;
import edu.cit.garbo.pawnscan.features.reports.repository.ReportRepository;
import edu.cit.garbo.pawnscan.features.reports.storage.FileStorageService;
import edu.cit.garbo.pawnscan.features.verification.entity.SearchLog;
import edu.cit.garbo.pawnscan.features.verification.entity.VerificationResult;
import edu.cit.garbo.pawnscan.features.verification.repository.SearchLogRepository;
import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ReportRepository reportRepository;
    private final ReportFileRepository reportFileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final SearchLogRepository searchLogRepository;

    @Override
    @Transactional
    public ReportResponse createReport(String authenticatedEmail, ReportUpsertRequest request) {
        User user = getAuthenticatedUser(authenticatedEmail);
        validateRequiredFields(request);

        String normalizedSerial = normalizeSerial(request.getSerialNumber());
        if (reportRepository.existsBySerialNumberIgnoreCase(normalizedSerial)) {
            throw new DuplicateSerialNumberException("Serial number already exists");
        }

        Report report = Report.builder()
                .serialNumber(normalizedSerial)
                .itemModel(request.getItemModel().trim())
                .description(request.getDescription().trim())
                .user(user)
                .build();

        Report savedReport = reportRepository.save(report);
        attachFileIfPresent(savedReport, request.getFile());

        return toResponse(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getUserReports(String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);

        return reportRepository.findByUserId(user.getUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchedReportResponse> getMatchedReports(String authenticatedEmail, int page, int size) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size));

        return searchLogRepository.findMatchedReportsForOwner(user.getUserId(), VerificationResult.STOLEN, pageable)
                .stream()
                .map(this::toMatchedReportResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReportResponse updateReport(String authenticatedEmail, Long reportId, ReportUpsertRequest request) {
        User user = getAuthenticatedUser(authenticatedEmail);
        validateRequiredFields(request);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found"));

        enforceOwnership(report, user.getUserId());

        String normalizedSerial = normalizeSerial(request.getSerialNumber());
        if (reportRepository.existsBySerialNumberIgnoreCaseAndIdNot(normalizedSerial, report.getId())) {
            throw new DuplicateSerialNumberException("Serial number already exists");
        }

        report.setSerialNumber(normalizedSerial);
        report.setItemModel(request.getItemModel().trim());
        report.setDescription(request.getDescription().trim());
        if (report.getStatus() == ReportStatus.REJECTED) {
            report.setStatus(ReportStatus.PENDING);
            report.setRejectionReason(null);
        }

        MultipartFile file = request.getFile();
        if (file != null && !file.isEmpty()) {
            attachFileIfPresent(report, file);
        }

        Report savedReport = reportRepository.save(report);
        return toResponse(savedReport);
    }

    @Override
    @Transactional
    public void deleteReport(String authenticatedEmail, Long reportId) {
        User user = getAuthenticatedUser(authenticatedEmail);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found"));

        enforceOwnership(report, user.getUserId());
        reportRepository.delete(report);
    }

    private User getAuthenticatedUser(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new ForbiddenActionException("Unauthorized access");
        }

        return userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ForbiddenActionException("Authenticated user was not found"));
    }

    private void enforceOwnership(Report report, Long requesterUserId) {
        if (!Objects.equals(report.getUser().getUserId(), requesterUserId)) {
            throw new ForbiddenActionException("You can only modify your own reports");
        }
    }

    private void validateRequiredFields(ReportUpsertRequest request) {
        if (request == null
                || isBlank(request.getSerialNumber())
                || isBlank(request.getItemModel())
                || isBlank(request.getDescription())) {
            throw new InvalidReportException("Serial number, item model, and description are required");
        }
    }

    private void attachFileIfPresent(Report report, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        ReportFileType fileType = resolveFileType(file);
        String fileUrl = fileStorageService.storeReportFile(file);

        ReportFile reportFile = ReportFile.builder()
                .report(report)
                .fileType(fileType)
                .fileUrl(fileUrl)
                .build();

        report.getFiles().add(reportFile);
        reportFileRepository.save(reportFile);
    }

    private ReportFileType resolveFileType(MultipartFile file) {
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        if (contentType.startsWith("image/")) {
            return ReportFileType.IMAGE;
        }

        if ("application/pdf".equals(contentType)) {
            return ReportFileType.PDF;
        }

        throw new InvalidReportException("Only image and PDF files are allowed");
    }

    private String normalizeSerial(String serialNumber) {
        return serialNumber == null ? "" : serialNumber.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private ReportResponse toResponse(Report report) {
        List<ReportFileResponse> files = report.getFiles().stream()
                .sorted(Comparator.comparing(ReportFile::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .map(file -> ReportFileResponse.builder()
                        .id(file.getId())
                        .fileUrl(file.getFileUrl())
                        .fileType(file.getFileType())
                        .build())
                .toList();

        return ReportResponse.builder()
                .id(report.getId())
                .serialNumber(report.getSerialNumber())
                .itemModel(report.getItemModel())
                .description(report.getDescription())
                .status(report.getStatus())
                .rejectionReason(report.getRejectionReason())
                .createdAt(report.getCreatedAt())
                .files(files)
                .build();
    }

    private MatchedReportResponse toMatchedReportResponse(SearchLog searchLog) {
        Report report = searchLog.getMatchedReport();
        User businessUser = searchLog.getBusinessUser();
        BusinessProfile businessProfile = businessUser == null ? null : businessUser.getBusinessProfile();

        List<ReportFileResponse> files = report.getFiles().stream()
                .sorted(Comparator.comparing(ReportFile::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .map(file -> ReportFileResponse.builder()
                        .id(file.getId())
                        .fileUrl(file.getFileUrl())
                        .fileType(file.getFileType())
                        .build())
                .toList();

        return MatchedReportResponse.builder()
                .matchId(searchLog.getId())
                .reportId(report.getId())
                .serialNumber(report.getSerialNumber())
                .itemModel(report.getItemModel())
                .description(report.getDescription())
                .status(report.getStatus())
                .reportCreatedAt(report.getCreatedAt())
                .matchedAt(searchLog.getSearchedAt())
                .matchedByBusinessName(businessProfile == null
                        ? (businessUser == null ? null : businessUser.getFullName())
                        : businessProfile.getBusinessName())
                .matchedByBusinessEmail(businessUser == null ? null : businessUser.getEmail())
                .matchedByBusinessPhone(businessUser == null ? null : businessUser.getPhoneNumber())
                .matchedByBusinessPermitNumber(businessProfile == null ? null : businessProfile.getPermitNumber())
                .matchedByBusinessAddress(businessProfile == null ? null : businessProfile.getBusinessAddress())
                .matchedByBusinessRegisteredAt(businessProfile == null ? null : businessProfile.getCreatedAt())
                .files(files)
                .build();
    }
}
