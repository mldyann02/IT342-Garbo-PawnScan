package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.AuthResponse;
import edu.cit.garbo.pawnscan.dto.LoginRequest;
import edu.cit.garbo.pawnscan.dto.RegisterRequest;
import edu.cit.garbo.pawnscan.entity.User;
import edu.cit.garbo.pawnscan.exception.EmailAlreadyExistsException;
import edu.cit.garbo.pawnscan.exception.InvalidCredentialsException;
import edu.cit.garbo.pawnscan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .message("User registered successfully")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}
