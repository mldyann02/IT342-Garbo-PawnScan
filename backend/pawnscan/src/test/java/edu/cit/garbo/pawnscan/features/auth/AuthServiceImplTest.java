package edu.cit.garbo.pawnscan.features.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import edu.cit.garbo.pawnscan.features.auth.dto.GoogleAuthRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.VerifyOtpRequest;
import edu.cit.garbo.pawnscan.features.auth.exception.InvalidCredentialsException;
import edu.cit.garbo.pawnscan.features.businessprofile.BusinessProfileService;
import edu.cit.garbo.pawnscan.shared.email.EmailService;
import edu.cit.garbo.pawnscan.shared.email.OtpService;
import edu.cit.garbo.pawnscan.shared.security.JwtService;
import edu.cit.garbo.pawnscan.shared.user.RegistrationStatus;
import edu.cit.garbo.pawnscan.shared.user.User;
import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import edu.cit.garbo.pawnscan.shared.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BusinessProfileService businessProfileService;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpService otpService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private GoogleIdToken googleIdToken;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                businessProfileService,
                jwtService,
                googleIdTokenVerifier,
                emailService,
                otpService,
                passwordResetTokenRepository);
        ReflectionTestUtils.setField(authService, "googleWebClientId", "google-client-id");
        ReflectionTestUtils.setField(authService, "baseUrl", "http://localhost:3000");
    }

    @Test
    void googleAuthenticationActivatesExistingPendingVerificationAccount() throws Exception {
        User pendingUser = User.builder()
                .userId(1L)
                .email("user@test.com")
                .fullName("Juan Dela Cruz")
                .passwordHash("hashed")
                .role(UserRole.USER)
                .registrationStatus(RegistrationStatus.PENDING_VERIFICATION)
                .build();

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
                .setEmail("user@test.com")
                .setEmailVerified(true)
                .setSubject("google-subject");
        payload.set("name", "Juan Dela Cruz");

        when(googleIdTokenVerifier.verify("google-token")).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(pendingUser)).thenReturn(pendingUser);
        when(jwtService.generateToken(pendingUser)).thenReturn("active-token");

        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setToken("google-token");
        request.setRole(UserRole.USER);

        var response = authService.authenticateWithGoogle(request);

        assertThat(pendingUser.getRegistrationStatus()).isEqualTo(RegistrationStatus.ACTIVE);
        assertThat(response.getRegistrationStatus()).isEqualTo(RegistrationStatus.ACTIVE);
        assertThat(response.getToken()).isEqualTo("active-token");
        verify(userRepository).save(pendingUser);
    }

    @Test
    void verifyOtpRejectsAccountsThatAreNotPendingVerification() {
        User incompleteUser = User.builder()
                .userId(2L)
                .email("google@test.com")
                .fullName("Google User")
                .role(UserRole.USER)
                .registrationStatus(RegistrationStatus.INCOMPLETE)
                .build();

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("google@test.com");
        request.setCode("123456");

        when(userRepository.findByEmail("google@test.com")).thenReturn(Optional.of(incompleteUser));

        assertThatThrownBy(() -> authService.verifyOtp(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("This account does not require email verification");

        verify(otpService, never()).verifyOtp(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }
}
