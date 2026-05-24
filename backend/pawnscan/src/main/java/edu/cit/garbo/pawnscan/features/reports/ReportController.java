package edu.cit.garbo.pawnscan.features.reports;

import edu.cit.garbo.pawnscan.features.reports.dto.MatchedReportResponse;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportResponse;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportUpsertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> createReport(
            Authentication authentication,
            @Valid @ModelAttribute ReportUpsertRequest request
    ) {
        ReportResponse created = reportService.createReport(resolveAuthenticatedName(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(Authentication authentication) {
        return ResponseEntity.ok(reportService.getUserReports(resolveAuthenticatedName(authentication)));
    }

    @GetMapping("/matched")
    public ResponseEntity<List<MatchedReportResponse>> getMatchedReports(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(reportService.getMatchedReports(resolveAuthenticatedName(authentication), page, size));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> updateReport(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @ModelAttribute ReportUpsertRequest request
    ) {
        return ResponseEntity.ok(reportService.updateReport(resolveAuthenticatedName(authentication), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(Authentication authentication, @PathVariable Long id) {
        reportService.deleteReport(resolveAuthenticatedName(authentication), id);
        return ResponseEntity.ok().build();
    }

    private String resolveAuthenticatedName(Authentication authentication) {
        Authentication currentAuthentication = authentication != null
                ? authentication
                : SecurityContextHolder.getContext().getAuthentication();

        if (currentAuthentication == null || currentAuthentication.getName() == null) {
            throw new IllegalStateException("Authentication is required");
        }

        return currentAuthentication.getName();
    }
}







