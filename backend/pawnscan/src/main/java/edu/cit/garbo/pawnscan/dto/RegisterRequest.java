package edu.cit.garbo.pawnscan.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import edu.cit.garbo.pawnscan.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @JsonAlias({"business_name", "businessName"})
    @Size(max = 255, message = "Business name must not exceed 255 characters")
    private String businessName;

    @JsonAlias({"business_address", "businessAddress"})
    @Size(max = 2000, message = "Business address must not exceed 2000 characters")
    private String businessAddress;

    @JsonAlias({"permit_number", "permitNumber"})
    @Size(max = 100, message = "Permit number must not exceed 100 characters")
    private String permitNumber;

    @NotNull(message = "Role is required")
    private UserRole role;
}
