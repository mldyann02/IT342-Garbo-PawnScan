package edu.cit.garbo.pawnscan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StolenReportSummaryResponse {

    private Long reportId;
    private String serialNumber;
    private String itemModel;
    private String description;
    private LocalDateTime dateReported;
}
