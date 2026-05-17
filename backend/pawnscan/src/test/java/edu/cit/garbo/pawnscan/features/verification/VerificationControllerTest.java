package edu.cit.garbo.pawnscan.features.verification;

import edu.cit.garbo.pawnscan.features.verification.dto.SearchLogResponse;
import edu.cit.garbo.pawnscan.features.verification.dto.StolenMatchResponse;
import edu.cit.garbo.pawnscan.features.verification.dto.StolenReportSummaryResponse;
import edu.cit.garbo.pawnscan.features.verification.dto.VerifySearchResponse;
import edu.cit.garbo.pawnscan.features.verification.entity.VerificationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Automated Tests for Verification Module (Search Engine)
 * Aligned with Full Regression Report Section 4.4.2
 * Corrected to satisfy DTO structures and bypass DB dependencies.
 */
@WebMvcTest(controllers = VerificationController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class VerificationControllerTest {

    private static final RequestPostProcessor BUSINESS_USER = user("business@test.com").roles("BUSINESS");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerificationService verificationService;

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    @DisplayName("TC-VERIFY-001: testSearchCleanSerial")
    void testSearchCleanSerial() throws Exception {
        VerifySearchResponse response = VerifySearchResponse.builder()
                .status(VerificationResult.CLEAN)
                .serial("SN-CLEAN-12345")
                .report(null)
                .build();

        // Ensure eq("business@test.com") matches the WithMockUser principal to avoid
        // NPE
        when(verificationService.verifySerial(eq("business@test.com"), eq("SN-CLEAN-12345")))
                .thenReturn(response);

        mockMvc.perform(get("/api/verify/search")
                .with(BUSINESS_USER)
                .param("serial", "SN-CLEAN-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLEAN"))
                .andExpect(jsonPath("$.serial").value("SN-CLEAN-12345"));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    @DisplayName("TC-VERIFY-002: testSearchStolenSerial")
    void testSearchStolenSerial() throws Exception {
        StolenReportSummaryResponse summary = StolenReportSummaryResponse.builder()
                .reportId(101L)
                .serialNumber("SN-STOLEN-999")
                .itemModel("iPhone 13")
                .build();

        VerifySearchResponse response = VerifySearchResponse.builder()
                .status(VerificationResult.STOLEN)
                .serial("SN-STOLEN-999")
                .report(summary)
                .build();

        when(verificationService.verifySerial(eq("business@test.com"), eq("SN-STOLEN-999")))
                .thenReturn(response);

        mockMvc.perform(get("/api/verify/search")
                .with(BUSINESS_USER)
                .param("serial", "SN-STOLEN-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STOLEN"))
                .andExpect(jsonPath("$.report.reportId").value(101));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    @DisplayName("TC-VERIFY-004: testGetSearchHistoryFirstPage")
    void testGetSearchHistoryFirstPage() throws Exception {
        SearchLogResponse log = SearchLogResponse.builder()
                .searchedSerial("SN-HISTORY-1")
                .result(VerificationResult.CLEAN)
                .timestamp(LocalDateTime.now())
                .build();

        when(verificationService.getSearchHistory(eq("business@test.com"), anyInt(), anyInt()))
                .thenReturn(List.of(log));

        mockMvc.perform(get("/api/verify/history")
                .with(BUSINESS_USER)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].searchedSerial").value("SN-HISTORY-1"));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    @DisplayName("TC-VERIFY-006: testGetStolenMatches")
    void testGetStolenMatches() throws Exception {
        StolenMatchResponse match = StolenMatchResponse.builder()
                .searchedSerial("SN-STOLEN-999")
                .matchedReportId(101L)
                .victimName("Melody Garbo")
                .build();

        when(verificationService.getStolenMatches(eq("business@test.com"), anyInt(), anyInt()))
                .thenReturn(List.of(match));

        mockMvc.perform(get("/api/verify/matches").with(BUSINESS_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].searchedSerial").value("SN-STOLEN-999"))
                .andExpect(jsonPath("$[0].matchedReportId").value(101));
    }

    @Test
    @WithMockUser(username = "business@test.com", roles = "BUSINESS")
    @DisplayName("TC-AUTH-010: testBusinessUserCanAccessVerification")
    void testBusinessUserCanAccessVerification() throws Exception {
        // Simple access check; service call is stubbed to prevent null response
        when(verificationService.verifySerial(eq("business@test.com"), eq("SN-12345")))
                .thenReturn(VerifySearchResponse.builder().build());

        mockMvc.perform(get("/api/verify/search").with(BUSINESS_USER).param("serial", "SN-12345"))
                .andExpect(status().isOk());
    }
}