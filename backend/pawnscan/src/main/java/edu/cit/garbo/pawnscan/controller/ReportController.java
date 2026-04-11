package edu.cit.garbo.pawnscan.controller;

import edu.cit.garbo.pawnscan.dto.ReportResponse;
import edu.cit.garbo.pawnscan.dto.ReportUpsertRequest;
import edu.cit.garbo.pawnscan.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        ReportResponse created = reportService.createReport(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(Authentication authentication) {
        return ResponseEntity.ok(reportService.getUserReports(authentication.getName()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> updateReport(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @ModelAttribute ReportUpsertRequest request
    ) {
        return ResponseEntity.ok(reportService.updateReport(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(Authentication authentication, @PathVariable Long id) {
        reportService.deleteReport(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }
}