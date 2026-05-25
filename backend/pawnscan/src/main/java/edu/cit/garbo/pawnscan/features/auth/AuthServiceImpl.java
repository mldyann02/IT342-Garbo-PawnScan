package edu.cit.garbo.pawnscan.features.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import edu.cit.garbo.pawnscan.features.auth.dto.AuthResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthConfigResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.CompleteProfileRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.LoginRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.RegisterRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.VerifyOtpRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.UserProfileResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.UserProfileUpdateRequest;
import edu.cit.garbo.pawnscan.features.auth.exception.EmailAlreadyExistsException;
import edu.cit.garbo.pawnscan.features.auth.exception.InvalidGoogleTokenException;
import edu.cit.garbo.pawnscan.features.auth.exception.InvalidCredentialsException;
import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileResponse;
import edu.cit.garbo.pawnscan.features.businessprofile.BusinessProfileService;
import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileRequest;
import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileSummaryResponse;
import edu.cit.garbo.pawnscan.features.businessprofile.exception.InvalidBusinessProfileException;
import edu.cit.garbo.pawnscan.shared.email.EmailService;
import edu.cit.garbo.pawnscan.shared.email.OtpService;
import edu.cit.garbo.pawnscan.shared.security.JwtService;
import edu.cit.garbo.pawnscan.shared.user.RegistrationStatus;
import edu.cit.garbo.pawnscan.shared.user.User;
import edu.cit.garbo.pawnscan.shared.user.UserRole;
import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String GOOGLE_PROVIDER = "GOOGLE";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessProfileService businessProfileService;
    private final JwtService jwtService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final EmailService emailService;
    private final OtpService otpService;

    @Value("${google.client-id:}")
    private String googleClientId;

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
                .registrationStatus(RegistrationStatus.PENDING_VERIFICATION)
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

        otpService.generateAndSendOtp(savedUser.getEmail());

        return AuthResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .registrationStatus(savedUser.getRegistrationStatus())
                .businessProfile(businessProfileSummary.orElse(null))
                .message("User registered successfully. Please check your email for the verification code.")
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

        if (user.getRegistrationStatus() == RegistrationStatus.PENDING_VERIFICATION) {
            throw new InvalidCredentialsException("Please verify your email address before logging in");
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
                .registrationStatus(user.getRegistrationStatus())
                .token(token)
                .businessProfile(businessProfileSummary.orElse(null))
                .message("Login successful")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse authenticateWithGoogle(GoogleAuthRequest request) {
        if (isBlank(googleClientId)) {
            throw new InvalidGoogleTokenException("Google authentication is not configured");
        }

        GoogleIdToken.Payload payload = verifyGoogleIdToken(request.getToken());
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        String googleSubject = payload.getSubject();

        if (isBlank(email)) {
            throw new InvalidGoogleTokenException("Google token did not include an email address");
        }

        boolean[] created = {false};
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    created[0] = true;
                    return userRepository.save(User.builder()
                            .email(email)
                            .fullName(isBlank(fullName) ? email : fullName)
                            .passwordHash(passwordEncoder.encode(generateFallbackCredential()))
                            .oauthProvider(GOOGLE_PROVIDER)
                            .oauthSubject(googleSubject)
                            .role(request.getRole() != null ? request.getRole() : UserRole.USER)
                            .registrationStatus(RegistrationStatus.INCOMPLETE)
                            .build());
                });

        if (!created[0]) {
            boolean updated = false;
            if (shouldLinkGoogleIdentity(user, googleSubject)) {
                user.setOauthProvider(GOOGLE_PROVIDER);
                user.setOauthSubject(googleSubject);
                updated = true;
            }
            if (user.getRegistrationStatus() == RegistrationStatus.PENDING_VERIFICATION) {
                user.setRegistrationStatus(RegistrationStatus.ACTIVE);
                updated = true;
            }
            if (user.getRegistrationStatus() == RegistrationStatus.INCOMPLETE 
                    && request.getRole() != null 
                    && user.getRole() != request.getRole()) {
                user.setRole(request.getRole());
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
            }
        }

        if (created[0]) {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        }

        return buildAuthResponse(user, "Google authentication successful");
    }

    @Override
    public GoogleAuthConfigResponse getGoogleAuthConfig() {
        return GoogleAuthConfigResponse.builder()
                .configured(!isBlank(googleClientId))
                .clientId(isBlank(googleClientId) ? null : googleClientId)
                .build();
    }

    private boolean shouldLinkGoogleIdentity(User user, String googleSubject) {
        if (isBlank(user.getOauthProvider()) || isBlank(user.getOauthSubject())) {
            return true;
        }

        if (GOOGLE_PROVIDER.equals(user.getOauthProvider()) && !googleSubject.equals(user.getOauthSubject())) {
            throw new InvalidGoogleTokenException("Google identity does not match the linked account");
        }

        return false;
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String token) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(token);
            if (idToken == null) {
                throw new InvalidGoogleTokenException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new InvalidGoogleTokenException("Google email address is not verified");
            }

            return payload;
        } catch (GeneralSecurityException | IOException ex) {
            throw new InvalidGoogleTokenException("Unable to verify Google token", ex);
        }
    }

    private String generateFallbackCredential() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private boolean hasBusinessRegistrationFields(RegisterRequest request) {
        return !isBlank(request.getBusinessName())
                && !isBlank(request.getBusinessAddress())
                && !isBlank(request.getPermitNumber());
    }

    @Override
    public AuthResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        Optional<BusinessProfileSummaryResponse> businessProfileSummary = user.getRole() == UserRole.BUSINESS
                ? businessProfileService.getSummaryByUserId(user.getUserId())
                : Optional.empty();

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .registrationStatus(user.getRegistrationStatus())
                .businessProfile(businessProfileSummary.orElse(null))
                .message("User details retrieved successfully")
                .build();
    }

    @Override
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        return toProfileResponse(user, "Profile retrieved successfully");
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (isBlank(request.getFullName())) {
            throw new InvalidBusinessProfileException("Full name is required");
        }

        user.setFullName(request.getFullName().trim());
        user.setPhoneNumber(normalizePhilippinePhone(request.getPhoneNumber()));
        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == UserRole.BUSINESS) {
            if (isBlank(request.getBusinessName())
                    || isBlank(request.getBusinessAddress())
                    || isBlank(request.getPermitNumber())) {
                throw new InvalidBusinessProfileException(
                        "Business name, business address, and permit number are required for BUSINESS users");
            }

            businessProfileService.updateProfile(savedUser.getUserId(), BusinessProfileRequest.builder()
                    .businessName(request.getBusinessName())
                    .businessAddress(request.getBusinessAddress())
                    .permitNumber(request.getPermitNumber())
                    .build(), savedUser.getUserId(), savedUser.getRole());
        }

        return toProfileResponse(savedUser, "Profile updated successfully");
    }

    @Override
    @Transactional
    public AuthResponse completeProfile(String email, CompleteProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (user.getRegistrationStatus() == RegistrationStatus.ACTIVE) {
            throw new InvalidBusinessProfileException("Profile is already complete");
        }

        user.setPhoneNumber(normalizePhilippinePhone(request.getPhoneNumber()));
        
        if (user.getRole() == UserRole.BUSINESS) {
            if (isBlank(request.getBusinessName())
                    || isBlank(request.getBusinessAddress())
                    || isBlank(request.getPermitNumber())) {
                throw new InvalidBusinessProfileException(
                        "Business name, business address, and permit number are required for BUSINESS users");
            }

            businessProfileService.createProfileForRegistration(user, BusinessProfileRequest.builder()
                    .businessName(request.getBusinessName())
                    .businessAddress(request.getBusinessAddress())
                    .permitNumber(request.getPermitNumber())
                    .build());
        }

        user.setRegistrationStatus(RegistrationStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser, "Profile completed successfully");
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (user.getRegistrationStatus() != RegistrationStatus.PENDING_VERIFICATION) {
            throw new InvalidCredentialsException("This account does not require email verification");
        }

        otpService.verifyOtp(request.getEmail(), request.getCode());

        user.setRegistrationStatus(RegistrationStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        return buildAuthResponse(savedUser, "Email verified successfully");
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        Optional<BusinessProfileSummaryResponse> businessProfileSummary = user.getRole() == UserRole.BUSINESS
                ? businessProfileService.getSummaryByUserId(user.getUserId())
                : Optional.empty();

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .registrationStatus(user.getRegistrationStatus())
                .token(token)
                .businessProfile(businessProfileSummary.orElse(null))
                .message(message)
                .build();
    }

    private UserProfileResponse toProfileResponse(User user, String message) {
        BusinessProfileResponse businessProfile = null;
        if (user.getRole() == UserRole.BUSINESS) {
            businessProfile = businessProfileService.getProfile(user.getUserId(), user.getUserId(), user.getRole());
        }

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .registrationStatus(user.getRegistrationStatus())
                .createdAt(user.getCreatedAt())
                .businessProfile(businessProfile)
                .message(message)
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
