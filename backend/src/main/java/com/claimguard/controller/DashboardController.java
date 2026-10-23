package com.claimguard.controller;

import com.claimguard.dto.response.ClaimResponse;
import com.claimguard.dto.response.ClaimsByStatusResponse;
import com.claimguard.dto.response.DashboardSummaryResponse;
import com.claimguard.dto.response.RiskDistributionResponse;
import com.claimguard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Aggregate statistics and metrics APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get aggregate metrics for dashboard stat cards")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        DashboardSummaryResponse summary = dashboardService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/claims-by-status")
    @Operation(summary = "Get claims grouped by status for distribution charts")
    public ResponseEntity<ClaimsByStatusResponse> getClaimsByStatus() {
        ClaimsByStatusResponse response = dashboardService.getClaimsByStatus();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/risk-distribution")
    @Operation(summary = "Get claims grouped by risk tier (LOW, MEDIUM, HIGH)")
    public ResponseEntity<RiskDistributionResponse> getRiskDistribution() {
        RiskDistributionResponse response = dashboardService.getRiskDistribution();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent-claims")
    @Operation(summary = "Get recent claims for dashboard activity feed")
    public ResponseEntity<List<ClaimResponse>> getRecentClaims(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<ClaimResponse> response = dashboardService.getRecentClaims(limit);
        return ResponseEntity.ok(response);
    }
}
