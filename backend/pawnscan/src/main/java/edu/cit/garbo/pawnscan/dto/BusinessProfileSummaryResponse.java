package edu.cit.garbo.pawnscan.dto;

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
public class BusinessProfileSummaryResponse {

    private Long userId;
    private String businessName;
    private boolean isVerified;
}