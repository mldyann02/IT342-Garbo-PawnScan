package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.ReportFileResponse;
import edu.cit.garbo.pawnscan.dto.ReportResponse;
import edu.cit.garbo.pawnscan.dto.ReportUpsertRequest;
import edu.cit.garbo.pawnscan.entity.Report;
import edu.cit.garbo.pawnscan.entity.ReportFile;
import edu.cit.garbo.pawnscan.entity.ReportFileType;
import edu.cit.garbo.pawnscan.entity.User;
import edu.cit.garbo.pawnscan.exception.DuplicateSerialNumberException;
import edu.cit.garbo.pawnscan.exception.ForbiddenActionException;
import edu.cit.garbo.pawnscan.exception.InvalidReportException;
import edu.cit.garbo.pawnscan.exception.ReportNotFoundException;
import edu.cit.garbo.pawnscan.repository.ReportFileRepository;
import edu.cit.garbo.pawnscan.repository.ReportRepository;
import edu.cit.garbo.pawnscan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    private final ReportRepository reportRepository;
    private final ReportFileRepository reportFileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

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

    private ReportResponse toResponse(Report report) {
        List<ReportFileResponse> files = report.getFiles().stream()
                .sorted(Comparator.comparing(ReportFile::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
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
                .createdAt(report.getCreatedAt())
                .files(files)
                .build();
    }
}