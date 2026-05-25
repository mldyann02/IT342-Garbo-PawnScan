package edu.cit.garbo.pawnscan.features.businessprofile.dto;

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
public class BusinessProfileResponse {

    private Long userId;
    private String businessName;
    private String businessAddress;
    private String permitNumber;
    private boolean isVerified;
    private boolean isRejected;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}







