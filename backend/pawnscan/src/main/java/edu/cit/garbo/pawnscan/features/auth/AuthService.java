package edu.cit.garbo.pawnscan.features.auth;

import edu.cit.garbo.pawnscan.features.auth.dto.AuthResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthConfigResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.LoginRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse authenticateWithGoogle(GoogleAuthRequest request);

    GoogleAuthConfigResponse getGoogleAuthConfig();

    AuthResponse getMe(String email);
}







