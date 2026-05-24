package edu.cit.garbo.pawnscan.features.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteProfileRequest {
    private String phoneNumber;
    private String businessName;
    private String businessAddress;
    private String permitNumber;
}
