package edu.cit.garbo.pawnscan.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
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
public class BusinessProfileRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 255, message = "Business name must not exceed 255 characters")
    @JsonAlias({"business_name", "businessName"})
    private String businessName;

    @NotBlank(message = "Business address is required")
    @Size(max = 2000, message = "Business address must not exceed 2000 characters")
    @JsonAlias({"business_address", "businessAddress"})
    private String businessAddress;

    @NotBlank(message = "Permit number is required")
    @Size(max = 100, message = "Permit number must not exceed 100 characters")
    @JsonAlias({"permit_number", "permitNumber"})
    private String permitNumber;
}