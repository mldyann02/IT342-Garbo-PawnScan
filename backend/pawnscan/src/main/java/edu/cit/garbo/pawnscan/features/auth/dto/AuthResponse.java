package edu.cit.garbo.pawnscan.features.auth.dto;

import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileSummaryResponse;
import edu.cit.garbo.pawnscan.shared.user.RegistrationStatus;
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
public class AuthResponse {

    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private RegistrationStatus registrationStatus;
    private String token;
    private BusinessProfileSummaryResponse businessProfile;
    private String message;
}
