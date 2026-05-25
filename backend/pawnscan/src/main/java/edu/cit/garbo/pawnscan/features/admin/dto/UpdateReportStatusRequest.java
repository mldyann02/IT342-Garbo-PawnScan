package edu.cit.garbo.pawnscan.features.admin.dto;

import edu.cit.garbo.pawnscan.features.reports.entity.ReportStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReportStatusRequest {
    private ReportStatus status;
    private String rejectionReason;
}
