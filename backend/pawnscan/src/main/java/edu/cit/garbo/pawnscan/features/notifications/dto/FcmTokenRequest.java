package edu.cit.garbo.pawnscan.features.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String token;

    private String platform;
}
