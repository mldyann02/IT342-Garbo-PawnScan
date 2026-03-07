package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.AuthResponse;
import edu.cit.garbo.pawnscan.dto.BusinessProfileRequest;
import edu.cit.garbo.pawnscan.dto.BusinessProfileSummaryResponse;
import edu.cit.garbo.pawnscan.dto.LoginRequest;
import edu.cit.garbo.pawnscan.dto.RegisterRequest;
import edu.cit.garbo.pawnscan.entity.User;
import edu.cit.garbo.pawnscan.entity.UserRole;
import edu.cit.garbo.pawnscan.exception.EmailAlreadyExistsException;
import edu.cit.garbo.pawnscan.exception.InvalidCredentialsException;
import edu.cit.garbo.pawnscan.exception.InvalidBusinessProfileException;
import edu.cit.garbo.pawnscan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import edu.cit.garbo.pawnscan.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessProfileService businessProfileService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        if (request.getRole() == UserRole.BUSINESS && !hasBusinessRegistrationFields(request)) {
            throw new InvalidBusinessProfileException(
                    "Business name, business address, and permit number are required for BUSINESS users");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(normalizePhilippinePhone(request.getPhoneNumber()))
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        Optional<BusinessProfileSummaryResponse> businessProfileSummary = Optional.empty();
        if (savedUser.getRole() == UserRole.BUSINESS) {
            businessProfileService.createProfileForRegistration(savedUser, BusinessProfileRequest.builder()
                    .businessName(request.getBusinessName())
                    .businessAddress(request.getBusinessAddress())
                    .permitNumber(request.getPermitNumber())
                    .build());

            businessProfileSummary = businessProfileService.getSummaryByUserId(savedUser.getUserId());
        }

        return AuthResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .businessProfile(businessProfileSummary.orElse(null))
                .message("User registered successfully")
                .build();
    }

    private String normalizePhilippinePhone(String input) {
        if (input == null)
            return null;
        String v = input.trim();
        if (v.isEmpty())
            return v;

        // Already normalized: +639XXXXXXXXX
        if (v.matches("^\\+639\\d{9}$"))
            return v;

        // Local 0XXXXXXXXXX -> +63XXXXXXXXXX
        if (v.matches("^0\\d{10}$"))
            return "+63" + v.substring(1);

        // Missing plus: 63XXXXXXXXXX -> +63XXXXXXXXXX
        if (v.matches("^63\\d{10}$"))
            return "+" + v;

        // Extract digits and try to normalize common patterns
        String digits = v.replaceAll("[^\\d]", "");
        if (digits.length() == 11 && digits.startsWith("09")) {
            return "+63" + digits.substring(1);
        }
        if (digits.length() == 12 && digits.startsWith("63")) {
            return "+" + digits;
        }

        // If nothing matched, return original trimmed input to allow validation to
        // catch it
        return v;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        Optional<BusinessProfileSummaryResponse> businessProfileSummary = user.getRole() == UserRole.BUSINESS
                ? businessProfileService.getSummaryByUserId(user.getUserId())
                : Optional.empty();

        // Generate JWT token including role
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .token(token)
                .businessProfile(businessProfileSummary.orElse(null))
                .message("Login successful")
                .build();
    }

    private boolean hasBusinessRegistrationFields(RegisterRequest request) {
        return !isBlank(request.getBusinessName())
                && !isBlank(request.getBusinessAddress())
                && !isBlank(request.getPermitNumber());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
