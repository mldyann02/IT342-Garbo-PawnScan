package edu.cit.garbo.pawnscan.features.verification.dto;

import edu.cit.garbo.pawnscan.features.verification.entity.VerificationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifySearchResponse {

    private VerificationResult status;
    private String serial;
    private StolenReportSummaryResponse report;
}







