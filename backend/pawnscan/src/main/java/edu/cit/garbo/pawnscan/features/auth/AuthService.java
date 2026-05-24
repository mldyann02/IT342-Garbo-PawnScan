package edu.cit.garbo.pawnscan.features.auth;

import edu.cit.garbo.pawnscan.features.auth.dto.AuthResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthConfigResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.LoginRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.RegisterRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.UserProfileResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.UserProfileUpdateRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse authenticateWithGoogle(GoogleAuthRequest request);

    GoogleAuthConfigResponse getGoogleAuthConfig();

    AuthResponse getMe(String email);

    UserProfileResponse getProfile(String email);

    UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request);
}







