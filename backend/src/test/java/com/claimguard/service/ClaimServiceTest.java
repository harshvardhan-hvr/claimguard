package com.claimguard.service;

import com.claimguard.dto.request.CreateClaimRequest;
import com.claimguard.dto.request.UpdateClaimRequest;
import com.claimguard.dto.response.ClaimDetailResponse;
import com.claimguard.entity.Claim;
import com.claimguard.entity.ClaimStatusHistory;
import com.claimguard.entity.RiskAssessment;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.RiskLevel;
import com.claimguard.exception.DuplicateResourceException;
import com.claimguard.exception.InvalidTransitionException;
import com.claimguard.repository.ClaimRepository;
import com.claimguard.repository.ClaimStatusHistoryRepository;
import com.claimguard.repository.RiskAssessmentRepository;
import com.claimguard.service.risk.RiskEngine;
import com.claimguard.service.risk.RiskEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private ClaimStatusHistoryRepository historyRepository;

    @Mock
    private RiskEngine riskEngine;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ClaimService claimService;

    private Claim testClaim;

    @BeforeEach
    void setUp() {
        testClaim = new Claim("CLM-2026-9999", "CLMT-TEST", BenefitCategory.HEALTHCARE,
                new BigDecimal("1500.00"), LocalDate.now(), "Test healthcare claim");
        testClaim.setId(10L);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException if custom claim reference already exists")
    void shouldThrowWhenClaimReferenceExists() {
        CreateClaimRequest request = new CreateClaimRequest("CLM-EXISTING", "CLMT-01",
                BenefitCategory.HOUSING, new BigDecimal("800.00"), LocalDate.now(), "Duplicate test");

        when(claimRepository.existsByClaimReference("CLM-EXISTING")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> claimService.createClaim(request));
    }

    @Test
    @DisplayName("Should reject updating an already CLEARED claim")
    void shouldRejectUpdatingFinalizedClaim() {
        testClaim.setStatus(ClaimStatus.CLEARED);
        when(claimRepository.findById(10L)).thenReturn(Optional.of(testClaim));

        UpdateClaimRequest updateReq = new UpdateClaimRequest(BenefitCategory.HEALTHCARE, new BigDecimal("2000.00"), "Update attempt");

        assertThrows(InvalidTransitionException.class, () -> claimService.updateClaim(10L, updateReq));
    }

    @Test
    @DisplayName("Should create claim with high risk, auto-flag, and persist assessment")
    void shouldCreateHighRiskClaimAndFlagForReview() {
        CreateClaimRequest request = new CreateClaimRequest("CLM-AUTO-01", "CLMT-HIGH",
                BenefitCategory.HEALTHCARE, new BigDecimal("8500.00"), LocalDate.now(), "High amount test");

        when(claimRepository.existsByClaimReference("CLM-AUTO-01")).thenReturn(false);
        when(claimRepository.findByClaimantId("CLMT-HIGH")).thenReturn(List.of());

        RiskEvaluationResult highRiskResult = new RiskEvaluationResult(
                75, RiskLevel.HIGH, List.of(), "High risk summary"
        );
        when(riskEngine.evaluate(any(), any())).thenReturn(highRiskResult);

        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> {
            Claim c = inv.getArgument(0);
            c.setId(20L);
            return c;
        });
        when(claimRepository.findById(20L)).thenReturn(Optional.of(testClaim));

        ClaimDetailResponse response = claimService.createClaim(request);

        assertNotNull(response);
        verify(riskAssessmentRepository).save(any(RiskAssessment.class));
        verify(historyRepository).save(any(ClaimStatusHistory.class));
        verify(auditService).logAction(eq("Claim"), eq(20L), eq("CREATE_CLAIM"), anyString());
    }
}
