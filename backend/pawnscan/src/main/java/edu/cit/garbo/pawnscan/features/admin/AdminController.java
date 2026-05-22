package edu.cit.garbo.pawnscan.features.admin;

import edu.cit.garbo.pawnscan.features.admin.dto.AdminStatsResponse;
import edu.cit.garbo.pawnscan.features.admin.dto.BusinessProfileAdminResponse;
import edu.cit.garbo.pawnscan.features.admin.dto.ReportAdminResponse;
import edu.cit.garbo.pawnscan.features.admin.dto.UpdateReportStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/reports/pending")
    public ResponseEntity<List<ReportAdminResponse>> getPendingReports() {
        return ResponseEntity.ok(adminService.getPendingReports());
    }

    @PatchMapping("/reports/{id}/status")
    public ResponseEntity<ReportAdminResponse> updateReportStatus(
            @PathVariable Long id,
            @RequestBody UpdateReportStatusRequest request
    ) {
        return ResponseEntity.ok(adminService.updateReportStatus(id, request.getStatus()));
    }

    @GetMapping("/businesses/verify")
    public ResponseEntity<List<BusinessProfileAdminResponse>> getPendingBusinesses() {
        return ResponseEntity.ok(adminService.getPendingBusinesses());
    }

    @GetMapping("/businesses")
    public ResponseEntity<List<BusinessProfileAdminResponse>> getAllBusinesses() {
        return ResponseEntity.ok(adminService.getAllBusinesses());
    }

    @PatchMapping("/businesses/{id}/verify")
    public ResponseEntity<BusinessProfileAdminResponse> verifyBusiness(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.verifyBusiness(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }
}
