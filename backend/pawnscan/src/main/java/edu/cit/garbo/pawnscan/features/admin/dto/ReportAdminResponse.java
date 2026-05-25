package edu.cit.garbo.pawnscan.features.admin.dto;

import edu.cit.garbo.pawnscan.features.reports.dto.ReportFileResponse;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportAdminResponse {
    private Long id;
    private String serialNumber;
    private String itemModel;
    private String description;
    private ReportStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private String ownerName;
    private String ownerEmail;
    private List<ReportFileResponse> files;
}
