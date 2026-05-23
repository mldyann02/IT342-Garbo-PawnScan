package edu.cit.garbo.pawnscan.features.reports.dto;

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
public class ReportResponse {

    private Long id;
    private String serialNumber;
    private String itemModel;
    private String description;
    private edu.cit.garbo.pawnscan.features.reports.entity.ReportStatus status;
    private LocalDateTime createdAt;
    private List<ReportFileResponse> files;
}







