package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.AuthResponse;
import edu.cit.garbo.pawnscan.dto.LoginRequest;
import edu.cit.garbo.pawnscan.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
