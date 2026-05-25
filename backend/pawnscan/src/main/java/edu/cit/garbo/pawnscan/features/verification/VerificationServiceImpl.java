package edu.cit.garbo.pawnscan.features.verification;

import edu.cit.garbo.pawnscan.features.businessprofile.entity.BusinessProfile;
import edu.cit.garbo.pawnscan.features.businessprofile.repository.BusinessProfileRepository;
import edu.cit.garbo.pawnscan.features.verification.dto.SearchLogResponse;
import edu.cit.garbo.pawnscan.features.verification.dto.StolenMatchResponse;
import edu.cit.garbo.pawnscan.features.verification.dto.StolenReportSummaryResponse;
import edu.cit.garbo.pawnscan.features.verification.dto.VerifySearchResponse;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportFileResponse;
import edu.cit.garbo.pawnscan.features.reports.entity.Report;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportFile;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportStatus;
import edu.cit.garbo.pawnscan.features.verification.entity.SearchLog;
import edu.cit.garbo.pawnscan.features.notifications.NotificationService;
import edu.cit.garbo.pawnscan.shared.user.User;
import edu.cit.garbo.pawnscan.shared.user.UserRole;
import edu.cit.garbo.pawnscan.features.verification.entity.VerificationResult;
import edu.cit.garbo.pawnscan.features.verification.publicapi.PublicStolenItemClient;
import edu.cit.garbo.pawnscan.features.verification.publicapi.PublicStolenItemMatch;
import edu.cit.garbo.pawnscan.shared.exception.ForbiddenActionException;
import edu.cit.garbo.pawnscan.features.verification.exception.InvalidVerificationRequestException;
import edu.cit.garbo.pawnscan.features.reports.repository.ReportRepository;
import edu.cit.garbo.pawnscan.features.verification.repository.SearchLogRepository;
import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import edu.cit.garbo.pawnscan.shared.validation.SerialNumberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationServiceImpl.class);

    private static final int MAX_PAGE_SIZE = 100;

    private final ReportRepository reportRepository;
    private final SearchLogRepository searchLogRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PublicStolenItemClient publicStolenItemClient;
    private final BusinessProfileRepository businessProfileRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('BUSINESS')")
    public VerifySearchResponse verifySerial(String authenticatedEmail, String serial) {
        VerificationAttempt attempt = performVerification(authenticatedEmail, serial);

        // Verify should still return a result even if log persistence fails.
        try {
            persistSearchLog(attempt.businessUser(), attempt.normalizedSerial(), attempt.result(),
                    attempt.matchedReport());
            notifyOriginalOwnerIfStolen(attempt.businessUser(), attempt.normalizedSerial(), attempt.matchedReport());
        } catch (RuntimeException ex) {
            LOGGER.warn("Search log persistence failed for business user {} and serial {}", authenticatedEmail,
                    attempt.normalizedSerial(), ex);
        }

        return VerifySearchResponse.builder()
                .status(attempt.result())
                .serial(attempt.normalizedSerial())
                .report(attempt.matchedReport() == null ? null : toStolenReportSummary(attempt.matchedReport()))
                .publicApiChecked(true)
                .publicApiStolen(attempt.publicMatch().stolen())
                .publicApiSource(attempt.publicMatch().source())
                .publicApiMatchTitle(attempt.publicMatch().title())
                .publicApiMatchUrl(attempt.publicMatch().url())
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('BUSINESS')")
    public SearchLogResponse logSearch(String authenticatedEmail, String serial) {
        VerificationAttempt attempt = performVerification(authenticatedEmail, serial);
        SearchLog log = persistSearchLog(
                attempt.businessUser(),
                attempt.normalizedSerial(),
                attempt.result(),
                attempt.matchedReport());
        notifyOriginalOwnerIfStolen(attempt.businessUser(), attempt.normalizedSerial(), attempt.matchedReport());
        return toSearchLogResponse(log);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('BUSINESS')")
    public java.util.List<SearchLogResponse> getSearchHistory(String authenticatedEmail, int page, int size) {
        User businessUser = getAuthenticatedBusinessUser(authenticatedEmail);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "searchedAt"));

        return searchLogRepository.findByBusinessUserUserId(businessUser.getUserId(), pageable)
                .stream()
                .map(this::toSearchLogResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('BUSINESS')")
    public java.util.List<StolenMatchResponse> getStolenMatches(String authenticatedEmail, int page, int size) {
        User businessUser = getAuthenticatedBusinessUser(authenticatedEmail);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "searchedAt"));

        return searchLogRepository.findByBusinessUserUserIdAndResult(
                businessUser.getUserId(),
                VerificationResult.STOLEN,
                pageable)
                .stream()
                .map(this::toStolenMatchResponse)
                .toList();
    }

    private User getAuthenticatedBusinessUser(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new ForbiddenActionException("Unauthorized access");
        }

        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ForbiddenActionException("Authenticated user was not found"));

        if (user.getRole() != UserRole.BUSINESS) {
            throw new ForbiddenActionException("Only BUSINESS users can verify item serial numbers");
        }

        BusinessProfile profile = businessProfileRepository.findById(user.getUserId())
                .orElseThrow(() -> new ForbiddenActionException("Business profile was not found"));

        if (!Boolean.TRUE.equals(profile.getIsVerified())) {
            throw new ForbiddenActionException("Only verified BUSINESS users can verify item serial numbers");
        }

        return user;
    }

    private VerificationAttempt performVerification(String authenticatedEmail, String serial) {
        User businessUser = getAuthenticatedBusinessUser(authenticatedEmail);
        String normalizedSerial = normalizeAndValidateSerial(serial);

        CompletableFuture<Optional<Report>> internalLookup = CompletableFuture.supplyAsync(() -> reportRepository
                .findFirstBySerialNumberIgnoreCaseAndStatusWithUser(normalizedSerial, ReportStatus.APPROVED));
        CompletableFuture<PublicStolenItemMatch> publicLookup = CompletableFuture
                .supplyAsync(() -> publicStolenItemClient.searchBySerial(normalizedSerial));

        Optional<Report> matchedReport = internalLookup.join();
        PublicStolenItemMatch publicMatch = publicLookup.join();
        VerificationResult result = matchedReport.isPresent() || publicMatch.stolen()
                ? VerificationResult.STOLEN
                : VerificationResult.CLEAN;

        return new VerificationAttempt(
                businessUser,
                normalizedSerial,
                matchedReport.orElse(null),
                result,
                publicMatch);
    }

    private SearchLog persistSearchLog(User businessUser, String normalizedSerial, VerificationResult result,
            Report matchedReport) {
        return searchLogRepository.save(SearchLog.builder()
                .searchedSerial(normalizedSerial)
                .result(result)
                .businessUser(businessUser)
                .matchedReport(matchedReport)
                .build());
    }

    private record VerificationAttempt(
            User businessUser,
            String normalizedSerial,
            Report matchedReport,
            VerificationResult result,
            PublicStolenItemMatch publicMatch) {
    }

    private void notifyOriginalOwnerIfStolen(User businessUser, String normalizedSerial, Report matchedReport) {
        if (matchedReport == null || matchedReport.getUser() == null) {
            return;
        }

        User owner = matchedReport.getUser();
        if (owner.getUserId().equals(businessUser.getUserId())) {
            return;
        }

        String businessName = businessUser.getFullName() == null || businessUser.getFullName().isBlank()
                ? "A verified business"
                : businessUser.getFullName();
        String itemModel = matchedReport.getItemModel() == null || matchedReport.getItemModel().isBlank()
                ? "your reported item"
                : matchedReport.getItemModel();

        notificationService.createNotification(
                owner,
                "Stolen item match found",
                businessName + " searched serial " + normalizedSerial + " and matched it to " + itemModel + ".",
                "/reports?tab=matched&reportId=" + matchedReport.getId());
    }

    private String normalizeAndValidateSerial(String serial) {
        if (serial == null || serial.trim().isEmpty()) {
            throw new InvalidVerificationRequestException("Serial number is required");
        }

        String normalized = SerialNumberValidator.normalize(serial);

        if (!SerialNumberValidator.isValid(normalized)) {
            throw new InvalidVerificationRequestException(SerialNumberValidator.ALLOWED_CHARACTERS_MESSAGE);
        }

        return normalized;
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private SearchLogResponse toSearchLogResponse(SearchLog log) {
        return SearchLogResponse.builder()
                .searchedSerial(log.getSearchedSerial())
                .itemModel(log.getMatchedReport() == null ? null : log.getMatchedReport().getItemModel())
                .result(log.getResult())
                .timestamp(log.getSearchedAt())
                .matchedReportId(log.getMatchedReport() == null ? null : log.getMatchedReport().getId())
                .build();
    }

    private StolenReportSummaryResponse toStolenReportSummary(Report report) {
        User victim = report.getUser();
        java.util.List<ReportFileResponse> files = report.getFiles().stream()
                .sorted(java.util.Comparator.comparing(ReportFile::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed())
                .map(file -> ReportFileResponse.builder()
                        .id(file.getId())
                        .fileUrl(file.getFileUrl())
                        .fileType(file.getFileType())
                        .build())
                .toList();

        return StolenReportSummaryResponse.builder()
                .reportId(report.getId())
                .serialNumber(report.getSerialNumber())
                .itemModel(report.getItemModel())
                .description(report.getDescription())
                .dateReported(report.getCreatedAt())
                .ownerName(victim == null ? null : victim.getFullName())
                .ownerEmail(victim == null ? null : victim.getEmail())
                .ownerPhoneNumber(victim == null ? null : victim.getPhoneNumber())
                .files(files)
                .build();
    }

    private StolenMatchResponse toStolenMatchResponse(SearchLog log) {
        Report report = log.getMatchedReport();

        if (report == null) {
            return StolenMatchResponse.builder()
                    .searchedSerial(log.getSearchedSerial())
                    .timestamp(log.getSearchedAt())
                    .matchedReportId(null)
                    .build();
        }

        User victim = report.getUser();
        java.util.List<ReportFileResponse> files = report.getFiles().stream()
                .sorted(java.util.Comparator.comparing(ReportFile::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed())
                .map(file -> ReportFileResponse.builder()
                        .id(file.getId())
                        .fileUrl(file.getFileUrl())
                        .fileType(file.getFileType())
                        .build())
                .toList();

        return StolenMatchResponse.builder()
                .searchedSerial(log.getSearchedSerial())
                .timestamp(log.getSearchedAt())
                .matchedReportId(report.getId())
                .itemModel(report.getItemModel())
                .description(report.getDescription())
                .dateReported(report.getCreatedAt())
                .victimName(victim == null ? null : victim.getFullName())
                .victimEmail(victim == null ? null : victim.getEmail())
                .victimPhoneNumber(victim == null ? null : victim.getPhoneNumber())
                .files(files)
                .build();
    }
}







