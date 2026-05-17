package edu.cit.garbo.pawnscan.features.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.garbo.pawnscan.features.auth.dto.AuthResponse;
import edu.cit.garbo.pawnscan.features.auth.dto.LoginRequest;
import edu.cit.garbo.pawnscan.features.auth.dto.RegisterRequest;
import edu.cit.garbo.pawnscan.shared.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Automated Tests for Authentication Module
 * Aligned with Full Regression Report Section 4.4.1
 */
@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("TC-AUTH-001: testRegisterIndividualSuccess")
    void testRegisterIndividualSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Juan Dela Cruz")
                .email("juan@test.com")
                .password("SecurePass123!")
                .phoneNumber("09171234567")
                .role(UserRole.USER)
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId(1L)
                .email("juan@test.com")
                .token("test-token")
                .message("User registered successfully")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    @DisplayName("TC-AUTH-002: testRegisterBusinessSuccess")
    void testRegisterBusinessSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Business Owner")
                .email("owner@shop.com")
                .password("SecurePass123!")
                .role(UserRole.BUSINESS)
                .businessName("Pawnstars Shop")
                .businessAddress("Cebu City")
                .permitNumber("PERMIT-001")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId(2L)
                .email("owner@shop.com")
                .role("BUSINESS")
                .message("User registered successfully")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("BUSINESS"));
    }

    @Test
    @DisplayName("TC-AUTH-006: testLoginSuccess")
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("juan@test.com")
                .password("SecurePass123!")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("login-token")
                .message("Login successful")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"));
    }

    @Test
    @DisplayName("TC-AUTH-003: testRegisterFailsWithInvalidEmail")
    void testRegisterFailsWithInvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test")
                .email("not-an-email")
                .password("Pass123!")
                .role(UserRole.USER)
                .build();

        // This should trigger @Valid in the Controller
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-AUTH-004: testRegisterFailsWithWeakPassword")
    void testRegisterFailsWithWeakPassword() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test")
                .email("test@email.com")
                .password("123")
                .role(UserRole.USER)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}