package com.claimguard.controller;

import com.claimguard.dto.request.CreateClaimRequest;
import com.claimguard.dto.request.StatusTransitionRequest;
import com.claimguard.dto.request.UpdateClaimRequest;
import com.claimguard.dto.response.*;
import com.claimguard.entity.Claim;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.RiskLevel;
import com.claimguard.service.ClaimService;
import com.claimguard.service.workflow.ClaimWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@Tag(name = "Claims Management", description = "Claims lifecycle, assessment, and review APIs")
public class ClaimController {

    private final ClaimService claimService;
    private final ClaimWorkflowService workflowService;

    public ClaimController(ClaimService claimService, ClaimWorkflowService workflowService) {
        this.claimService = claimService;
        this.workflowService = workflowService;
    }

    @GetMapping
    @Operation(summary = "Search and filter claims with pagination and sorting")
    public ResponseEntity<PagedResponse<ClaimResponse>> getClaims(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) BenefitCategory category,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PagedResponse<ClaimResponse> response = claimService.getClaims(
                search, status, category, riskLevel, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new benefit claim with immediate risk scoring")
    public ResponseEntity<ClaimDetailResponse> createClaim(@Valid @RequestBody CreateClaimRequest request) {
        ClaimDetailResponse response = claimService.createClaim(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full details of a specific claim")
    public ResponseEntity<ClaimDetailResponse> getClaimDetails(@PathVariable Long id) {
        ClaimDetailResponse response = claimService.getClaimDetails(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update eligible fields of an active claim")
    public ResponseEntity<ClaimDetailResponse> updateClaim(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClaimRequest request
    ) {
        ClaimDetailResponse response = claimService.updateClaim(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get status change history timeline for a claim")
    public ResponseEntity<List<StatusHistoryResponse>> getClaimHistory(@PathVariable Long id) {
        List<StatusHistoryResponse> history = workflowService.getStatusHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/risk-assessment")
    @Operation(summary = "Get detailed risk assessment and rule explanations for a claim")
    public ResponseEntity<RiskAssessmentResponse> getClaimRiskAssessment(@PathVariable Long id) {
        RiskAssessmentResponse assessment = claimService.getLatestRiskAssessment(id);
        return ResponseEntity.ok(assessment);
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Re-evaluate and recalculate risk score for a claim")
    public ResponseEntity<RiskAssessmentResponse> analyzeClaim(@PathVariable Long id) {
        RiskAssessmentResponse assessment = claimService.analyzeClaim(id);
        return ResponseEntity.ok(assessment);
    }

    @PostMapping("/{id}/status-transitions")
    @Operation(summary = "Transition claim status with mandatory justification and role enforcement")
    public ResponseEntity<ClaimDetailResponse> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusTransitionRequest request
    ) {
        workflowService.updateStatus(id, request);
        ClaimDetailResponse updatedClaim = claimService.getClaimDetails(id);
        return ResponseEntity.ok(updatedClaim);
    }
}
