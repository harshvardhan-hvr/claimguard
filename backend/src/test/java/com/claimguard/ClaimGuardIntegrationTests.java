package com.claimguard;

import com.claimguard.dto.request.LoginRequest;
import com.claimguard.dto.request.StatusTransitionRequest;
import com.claimguard.enums.ClaimStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimGuardIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtainJwtToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseNode.get("token").asText();
    }

    @Test
    @DisplayName("Integration: Authenticate with seeded reviewer credentials and retrieve current user")
    void shouldLoginAndGetProfile() throws Exception {
        String token = obtainJwtToken("reviewer1", "password123");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonNode -> {
                    // Check username and role
                })
                .andExpect(jsonPath("$.username").value("reviewer1"))
                .andExpect(jsonPath("$.role").value("CLAIMS_REVIEWER"));
    }

    @Test
    @DisplayName("Integration: Reject unauthenticated request to /api/claims")
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/claims"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integration: Filter seeded claims by status and category")
    void shouldFilterClaims() throws Exception {
        String token = obtainJwtToken("reviewer1", "password123");

        mockMvc.perform(get("/api/claims")
                        .param("status", "CLEARED")
                        .param("category", "HEALTHCARE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[0].category").value("HEALTHCARE"))
                .andExpect(jsonPath("$.content[0].status").value("CLEARED"));
    }

    @Test
    @DisplayName("Integration: Dashboard statistics aggregate properly from database")
    void shouldRetrieveDashboardSummary() throws Exception {
        String token = obtainJwtToken("reviewer1", "password123");

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClaims", greaterThanOrEqualTo(30)))
                .andExpect(jsonPath("$.highRiskClaims", greaterThan(0)))
                .andExpect(jsonPath("$.reviewedClaims", greaterThan(0)));
    }

    @Test
    @DisplayName("Integration: Dashboard claims by status returns map of all statuses")
    void shouldRetrieveClaimsByStatus() throws Exception {
        String token = obtainJwtToken("reviewer1", "password123");

        mockMvc.perform(get("/api/dashboard/claims-by-status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCounts.CLEARED", greaterThan(0)))
                .andExpect(jsonPath("$.statusCounts.CONFIRMED_FRAUD", greaterThan(0)));
    }

    @Test
    @DisplayName("Integration: End-to-end investigation workflow: Reviewer transitions SUBMITTED claim to UNDER_REVIEW")
    void shouldExecuteStatusTransition() throws Exception {
        String token = obtainJwtToken("reviewer1", "password123");

        // Claim ID 6 is SUBMITTED in seed data
        StatusTransitionRequest transitionRequest = new StatusTransitionRequest(
                ClaimStatus.UNDER_REVIEW,
                "Claim assigned to Sarah Connor for dental invoice verification"
        );

        mockMvc.perform(post("/api/claims/6/status-transitions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transitionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.statusHistory", not(empty())));
    }

    @Test
    @DisplayName("Integration: Non-admin reviewer cannot access Admin user management APIs")
    void shouldDenyReviewerFromAdminEndpoints() throws Exception {
        String reviewerToken = obtainJwtToken("reviewer1", "password123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + reviewerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integration: Admin user can access Admin user management APIs")
    void shouldAllowAdminAccessToUserManagement() throws Exception {
        String adminToken = obtainJwtToken("admin", "password123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }
}
