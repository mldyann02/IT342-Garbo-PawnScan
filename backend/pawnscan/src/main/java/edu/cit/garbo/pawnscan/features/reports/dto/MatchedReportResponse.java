package edu.cit.garbo.pawnscan.features.reports.dto;

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
public class MatchedReportResponse {

    private Long matchId;
    private Long reportId;
    private String serialNumber;
    private String itemModel;
    private String description;
    private ReportStatus status;
    private LocalDateTime reportCreatedAt;
    private LocalDateTime matchedAt;
    private String matchedByBusinessName;
    private String matchedByBusinessEmail;
    private String matchedByBusinessPhone;
    private String matchedByBusinessPermitNumber;
    private String matchedByBusinessAddress;
    private LocalDateTime matchedByBusinessRegisteredAt;
    private List<ReportFileResponse> files;
}
