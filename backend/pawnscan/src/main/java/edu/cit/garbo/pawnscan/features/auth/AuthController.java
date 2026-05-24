package edu.cit.garbo.pawnscan.features.auth;

import edu.cit.garbo.pawnscan.features.auth.dto.AuthResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthConfigResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.LoginRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.RegisterRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.UserProfileResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.UserProfileUpdateRequest;
import edu.cit.garbo.pawnscan.features.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        AuthResponse response = authService.authenticateWithGoogle(request);
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.GetMapping("/google/config")
    public ResponseEntity<GoogleAuthConfigResponse> getGoogleAuthConfig() {
        return ResponseEntity.ok(authService.getGoogleAuthConfig());
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse response = authService.getMe(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(authService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            org.springframework.security.core.Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(authService.updateProfile(authentication.getName(), request));
    }
}








