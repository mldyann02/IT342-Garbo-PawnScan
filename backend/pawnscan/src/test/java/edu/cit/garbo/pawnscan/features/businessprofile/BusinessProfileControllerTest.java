package edu.cit.garbo.pawnscan.features.businessprofile;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileRequest;
import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileResponse;
import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessVerificationRequest;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Automated Tests for Business Profile Module
 * Aligned with Full Regression Report Section 4.4.4
 */
@WebMvcTest(controllers = BusinessProfileController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class BusinessProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessProfileService businessProfileService;

    @Test
    @DisplayName("TC-PROFILE-001: testCreateBusinessProfile")
    void testCreateBusinessProfile() throws Exception {
        BusinessProfileRequest request = BusinessProfileRequest.builder()
                .businessName("Melody Pawnshop")
                .businessAddress("Cebu City, Philippines")
                .permitNumber("BP-2026-0001")
                .build();

        BusinessProfileResponse response = BusinessProfileResponse.builder()
                .userId(10L)
                .businessName("Melody Pawnshop")
                .isVerified(false)
                .build();

        // Matching service signature: Long targetUserId, BusinessProfileRequest
        // request, Long actorUserId, UserRole actorRole
        when(businessProfileService.createProfile(eq(10L), any(BusinessProfileRequest.class), eq(1L),
                eq(UserRole.ADMIN)))
                .thenReturn(response);

        mockMvc.perform(post("/api/business-profiles/{userId}", 10L)
                .header("X-Requester-User-Id", "1")
                .header("X-Requester-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessName").value("Melody Pawnshop"));
    }

    @Test
    @DisplayName("TC-PROFILE-003: testGetAllProfiles")
    void testGetAllProfiles() throws Exception {
        BusinessProfileResponse response = BusinessProfileResponse.builder()
                .userId(10L)
                .businessName("Verified Pawnshop")
                .isVerified(true)
                .build();

        when(businessProfileService.getProfiles(eq(null), eq(UserRole.ADMIN)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/business-profiles")
                .header("X-Requester-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessName").value("Verified Pawnshop"));
    }

    @Test
    @DisplayName("TC-PROFILE-004: testFilterProfilesByVerification")
    void testFilterProfilesByVerification() throws Exception {
        BusinessProfileResponse response = BusinessProfileResponse.builder()
                .userId(10L)
                .businessName("Verified Pawnshop")
                .isVerified(true)
                .build();

        when(businessProfileService.getProfiles(eq(true), eq(UserRole.ADMIN)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/business-profiles")
                .param("isVerified", "true")
                .header("X-Requester-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].verified").value(true));
    }

    @Test
    @DisplayName("TC-PROFILE-002: testUpdateProfile")
    void testUpdateProfile() throws Exception {
        // Must include all @NotBlank fields to avoid 400 Bad Request
        BusinessProfileRequest request = BusinessProfileRequest.builder()
                .businessName("Updated Name")
                .businessAddress("New Address")
                .permitNumber("BP-NEW-999")
                .build();

        BusinessProfileResponse response = BusinessProfileResponse.builder()
                .businessName("Updated Name")
                .build();

        when(businessProfileService.updateProfile(eq(10L), any(BusinessProfileRequest.class), eq(10L),
                eq(UserRole.BUSINESS)))
                .thenReturn(response);

        mockMvc.perform(put("/api/business-profiles/{userId}", 10L)
                .header("X-Requester-User-Id", "10")
                .header("X-Requester-Role", "BUSINESS")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Updated Name"));
    }

    @Test
    @DisplayName("TC-PROFILE-005: testUpdateVerificationStatus")
    void testUpdateVerificationStatus() throws Exception {
        BusinessVerificationRequest request = BusinessVerificationRequest.builder()
                .isVerified(true)
                .build();

        BusinessProfileResponse response = BusinessProfileResponse.builder()
                .isVerified(true)
                .build();

        when(businessProfileService.updateVerification(eq(10L), eq(true), eq(UserRole.ADMIN)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/business-profiles/{userId}/verification", 10L)
                .header("X-Requester-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("TC-PROFILE-006: testDeleteProfile")
    void testDeleteProfile() throws Exception {
        doNothing().when(businessProfileService).deleteProfile(eq(10L), eq(1L), eq(UserRole.ADMIN));

        mockMvc.perform(delete("/api/business-profiles/{userId}", 10L)
                .header("X-Requester-User-Id", "1")
                .header("X-Requester-Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }
}