package edu.cit.garbo.pawnscan.dto;

import jakarta.validation.constraints.NotNull;
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
public class BusinessVerificationRequest {

    @NotNull(message = "Verification status is required")
    private Boolean isVerified;
}