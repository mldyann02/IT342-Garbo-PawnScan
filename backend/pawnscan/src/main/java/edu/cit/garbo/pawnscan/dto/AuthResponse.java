package edu.cit.garbo.pawnscan.dto;

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
    private String token;
    private BusinessProfileSummaryResponse businessProfile;
    private String message;
}
