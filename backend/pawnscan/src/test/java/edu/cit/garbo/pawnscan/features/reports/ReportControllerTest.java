package edu.cit.garbo.pawnscan.features.reports;

import edu.cit.garbo.pawnscan.features.reports.dto.ReportResponse;
import edu.cit.garbo.pawnscan.features.reports.dto.ReportUpsertRequest;
import edu.cit.garbo.pawnscan.features.reports.exception.InvalidReportException;
import edu.cit.garbo.pawnscan.shared.exception.ForbiddenActionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Automated Tests for Reports Module (Stolen Item Registry)
 * Corrected to bypass database dependencies and satisfy DTO validation.
 */
@WebMvcTest(controllers = ReportController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    private static final RequestPostProcessor USER = user("user@test.com").roles("USER");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("TC-REPORT-001: testCreateReportSuccess")
    void testCreateReportSuccess() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .id(1L)
                .serialNumber("SN-12345")
                .itemModel("iPhone 13")
                .build();

        // Match the service call
        when(reportService.createReport(eq("user@test.com"), any(ReportUpsertRequest.class)))
                .thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file", "evidence.pdf", "application/pdf", "test data".getBytes());

        // Fields must match @NotBlank constraints in ReportUpsertRequest
        mockMvc.perform(multipart("/api/reports")
                .file(file)
                .param("serialNumber", "SN-12345")
                .param("itemModel", "iPhone 13")
                .param("description", "Stolen near the park")
                .with(USER)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value("SN-12345"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("TC-REPORT-005: testGetUserReports")
    void testGetUserReports() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .id(1L)
                .serialNumber("SN-12345")
                .build();

        when(reportService.getUserReports("user@test.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/reports").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serialNumber").value("SN-12345"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("TC-REPORT-006: testUpdateReport")
    void testUpdateReport() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .id(1L)
                .itemModel("iPhone 13 Pro")
                .build();

        when(reportService.updateReport(eq("user@test.com"), eq(1L), any(ReportUpsertRequest.class)))
                .thenReturn(response);

        // Standard PUT doesn't support multipart params easily, so we use multipart() +
        // setMethod
        mockMvc.perform(multipart("/api/reports/1")
                .param("serialNumber", "SN-12345")
                .param("itemModel", "iPhone 13 Pro")
                .param("description", "Updated description")
                .with(USER)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemModel").value("iPhone 13 Pro"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("TC-REPORT-007: testDeleteReport")
    void testDeleteReport() throws Exception {
        doNothing().when(reportService).deleteReport("user@test.com", 1L);

        mockMvc.perform(delete("/api/reports/1").with(USER))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "USER")
    @DisplayName("TC-REPORT-008: testCannotModifyOthersReport")
    void testCannotModifyOthersReport() throws Exception {
        // Simulates the ownership enforcement in ReportServiceImpl
        doThrow(new ForbiddenActionException("You can only modify your own reports"))
                .when(reportService).deleteReport(eq("other@test.com"), eq(1L));

        mockMvc.perform(delete("/api/reports/1").with(user("other@test.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("TC-REPORT-003: testRejectInvalidFileType")
    void testRejectInvalidFileType() throws Exception {
        // MockMultipartFile representing an illegal executable
        MockMultipartFile illegalFile = new MockMultipartFile(
                "file", "malware.exe", "application/x-msdownload", "binary".getBytes());

        doThrow(new InvalidReportException("Only image and PDF files are allowed"))
                .when(reportService).createReport(eq("user@test.com"), any(ReportUpsertRequest.class));

        mockMvc.perform(multipart("/api/reports")
                .file(illegalFile)
                .param("serialNumber", "SN-BAD")
                .param("itemModel", "Virus")
                .param("description", "Illegal upload attempt")
                .with(USER))
                .andExpect(status().isBadRequest());
    }
}