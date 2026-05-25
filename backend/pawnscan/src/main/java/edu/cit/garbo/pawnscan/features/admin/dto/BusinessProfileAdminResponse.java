package edu.cit.garbo.pawnscan.features.admin.dto;

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
public class BusinessProfileAdminResponse {
    private Long userId;
    private String businessName;
    private String businessAddress;
    private String permitNumber;
    private Boolean isVerified;
    private Boolean isRejected;
    private String rejectionReason;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
}
