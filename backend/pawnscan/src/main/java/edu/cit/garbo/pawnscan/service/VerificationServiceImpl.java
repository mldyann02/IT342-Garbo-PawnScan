package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.SearchLogResponse;
import edu.cit.garbo.pawnscan.dto.StolenMatchResponse;
import edu.cit.garbo.pawnscan.dto.StolenReportSummaryResponse;
import edu.cit.garbo.pawnscan.dto.VerifySearchResponse;
import edu.cit.garbo.pawnscan.entity.Report;
import edu.cit.garbo.pawnscan.entity.ReportFile;
import edu.cit.garbo.pawnscan.entity.ReportStatus;
import edu.cit.garbo.pawnscan.entity.SearchLog;
import edu.cit.garbo.pawnscan.entity.User;
import edu.cit.garbo.pawnscan.entity.UserRole;
import edu.cit.garbo.pawnscan.entity.VerificationResult;
import edu.cit.garbo.pawnscan.exception.ForbiddenActionException;
import edu.cit.garbo.pawnscan.exception.InvalidVerificationRequestException;
import edu.cit.garbo.pawnscan.repository.ReportRepository;
import edu.cit.garbo.pawnscan.repository.SearchLogRepository;
import edu.cit.garbo.pawnscan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationServiceImpl.class);

    private static final int MAX_PAGE_SIZE = 100;
    private static final Pattern SERIAL_PATTERN = Pattern.compile("^[A-Z0-9][A-Z0-9\\-_.:/]{1,63}$");

    private final ReportRepository reportRepository;
    private final SearchLogRepository searchLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('BUSINESS')")
    public VerifySearchResponse verifySerial(String authenticatedEmail, String serial) {
        VerificationAttempt attempt = performVerification(authenticatedEmail, serial);

        // Verify should still return a result even if log persistence fails.
        try {
            persistSearchLog(attempt.businessUser(), attempt.normalizedSerial(), attempt.result(),
                    attempt.matchedReport());
        } catch (RuntimeException ex) {
            LOGGER.warn("Search log persistence failed for business user {} and serial {}", authenticatedEmail,
                    attempt.normalizedSerial(), ex);
        }

        return VerifySearchResponse.builder()
                .status(attempt.result())
                .serial(attempt.normalizedSerial())
                .report(attempt.matchedReport() == null ? null : toStolenReportSummary(attempt.matchedReport()))
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

        return user;
    }

    private VerificationAttempt performVerification(String authenticatedEmail, String serial) {
        User businessUser = getAuthenticatedBusinessUser(authenticatedEmail);
        String normalizedSerial = normalizeAndValidateSerial(serial);

        Optional<Report> matchedReport = reportRepository
                .findFirstBySerialNumberIgnoreCaseAndStatus(normalizedSerial, ReportStatus.APPROVED);

        VerificationResult result = matchedReport.isPresent() ? VerificationResult.STOLEN : VerificationResult.CLEAN;

        return new VerificationAttempt(
                businessUser,
                normalizedSerial,
                matchedReport.orElse(null),
                result);
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
            VerificationResult result) {
    }

    private String normalizeAndValidateSerial(String serial) {
        if (serial == null || serial.trim().isEmpty()) {
            throw new InvalidVerificationRequestException("Serial number is required");
        }

        String normalized = serial.trim().toUpperCase(Locale.ROOT);

        if (!SERIAL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidVerificationRequestException("Serial number format is invalid");
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
                .result(log.getResult())
                .timestamp(log.getSearchedAt())
                .matchedReportId(log.getMatchedReport() == null ? null : log.getMatchedReport().getId())
                .build();
    }

    private StolenReportSummaryResponse toStolenReportSummary(Report report) {
        return StolenReportSummaryResponse.builder()
                .reportId(report.getId())
                .serialNumber(report.getSerialNumber())
                .itemModel(report.getItemModel())
                .description(report.getDescription())
                .dateReported(report.getCreatedAt())
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
        ReportFile evidence = report.getFiles() == null || report.getFiles().isEmpty()
                ? null
                : report.getFiles().get(0);

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
                .evidenceFileUrl(evidence == null ? null : evidence.getFileUrl())
                .evidenceFileType(evidence == null || evidence.getFileType() == null
                        ? null
                        : evidence.getFileType().name())
                .build();
    }
}
