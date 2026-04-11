package edu.cit.garbo.pawnscan.dto;

import edu.cit.garbo.pawnscan.entity.VerificationResult;
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
public class SearchLogResponse {

    private String searchedSerial;
    private VerificationResult result;
    private LocalDateTime timestamp;
    private Long matchedReportId;
}
