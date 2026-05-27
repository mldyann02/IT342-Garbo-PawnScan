package edu.cit.garbo.pawnscan.features.auth.dto;

import edu.cit.garbo.pawnscan.shared.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthRequest {

    @NotBlank(message = "Google token is required")
    private String token;

    private UserRole role;
}
