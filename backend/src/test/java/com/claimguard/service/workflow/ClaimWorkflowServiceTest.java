package com.claimguard.service.workflow;

import com.claimguard.dto.request.StatusTransitionRequest;
import com.claimguard.entity.Claim;
import com.claimguard.entity.ClaimStatusHistory;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.Role;
import com.claimguard.exception.ResourceNotFoundException;
import com.claimguard.repository.ClaimRepository;
import com.claimguard.repository.ClaimStatusHistoryRepository;
import com.claimguard.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimWorkflowServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimStatusHistoryRepository historyRepository;

    @Mock
    private StatusTransitionValidator validator;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ClaimWorkflowService workflowService;

    private Claim sampleClaim;

    @BeforeEach
    void setUp() {
        sampleClaim = new Claim("CLM-WF-01", "CLMT-01", BenefitCategory.HEALTHCARE,
                new BigDecimal("1200.00"), LocalDate.now(), "Test claim");
        sampleClaim.setId(99L);
        sampleClaim.setStatus(ClaimStatus.SUBMITTED);
    }

    @Test
    @DisplayName("Should successfully transition status, persist history, and audit log")
    void shouldUpdateStatusSuccessfully() {
        when(claimRepository.findById(99L)).thenReturn(Optional.of(sampleClaim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StatusTransitionRequest request = new StatusTransitionRequest(
                ClaimStatus.UNDER_REVIEW,
                "Case assigned to specialized reviewer for invoice checks"
        );

        Claim updated = workflowService.updateStatus(99L, request);

        assertEquals(ClaimStatus.UNDER_REVIEW, updated.getStatus());
        verify(validator).validateTransition(eq(ClaimStatus.SUBMITTED), eq(ClaimStatus.UNDER_REVIEW), any(), anyString());
        verify(historyRepository).save(any(ClaimStatusHistory.class));
        verify(claimRepository).save(sampleClaim);
        verify(auditService).logAction(eq("Claim"), eq(99L), eq("STATUS_TRANSITION"), anyString());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transitioning non-existent claim")
    void shouldThrowWhenClaimNotFound() {
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        StatusTransitionRequest request = new StatusTransitionRequest(ClaimStatus.UNDER_REVIEW, "Valid reason here");

        assertThrows(ResourceNotFoundException.class, () -> workflowService.updateStatus(999L, request));
        verifyNoInteractions(historyRepository);
    }
}
