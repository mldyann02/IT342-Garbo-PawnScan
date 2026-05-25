package edu.cit.garbo.pawnscan.features.auth.dto;

import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileResponse;
import edu.cit.garbo.pawnscan.shared.user.RegistrationStatus;
import java.time.LocalDateTime;
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
public class UserProfileResponse {

    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private RegistrationStatus registrationStatus;
    private LocalDateTime createdAt;
    private BusinessProfileResponse businessProfile;
    private String message;
}
