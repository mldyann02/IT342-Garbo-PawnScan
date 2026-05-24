package edu.cit.garbo.pawnscan.features.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UserProfileUpdateRequest {

    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^$|^(\\+63|0)9\\d{9}$", message = "Phone number must be a valid Philippine mobile number (e.g. +639171234567 or 09171234567)")
    private String phoneNumber;

    @Size(max = 255, message = "Business name must not exceed 255 characters")
    private String businessName;

    @Size(max = 2000, message = "Business address must not exceed 2000 characters")
    private String businessAddress;

    @Size(max = 100, message = "Permit number must not exceed 100 characters")
    private String permitNumber;
}
