package com.claimguard.service.workflow;

import com.claimguard.dto.request.StatusTransitionRequest;
import com.claimguard.dto.response.ClaimDetailResponse;
import com.claimguard.dto.response.StatusHistoryResponse;
import com.claimguard.entity.Claim;
import com.claimguard.entity.ClaimStatusHistory;
import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.Role;
import com.claimguard.exception.ResourceNotFoundException;
import com.claimguard.repository.ClaimRepository;
import com.claimguard.repository.ClaimStatusHistoryRepository;
import com.claimguard.security.SecurityUtils;
import com.claimguard.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimWorkflowService {

    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository historyRepository;
    private final StatusTransitionValidator validator;
    private final AuditService auditService;

    public ClaimWorkflowService(ClaimRepository claimRepository,
                                ClaimStatusHistoryRepository historyRepository,
                                StatusTransitionValidator validator,
                                AuditService auditService) {
        this.claimRepository = claimRepository;
        this.historyRepository = historyRepository;
        this.validator = validator;
        this.auditService = auditService;
    }

    @Transactional
    public Claim updateStatus(Long claimId, StatusTransitionRequest request) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        ClaimStatus fromStatus = claim.getStatus();
        ClaimStatus toStatus = request.getToStatus();
        String reason = request.getReason();

        Role currentUserRole = SecurityUtils.getCurrentUserRole();
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // Validate state machine rule
        validator.validateTransition(fromStatus, toStatus, currentUserRole, reason);

        // Record history
        ClaimStatusHistory history = new ClaimStatusHistory(
                claim,
                fromStatus,
                toStatus,
                currentUserId,
                currentUsername,
                reason.trim()
        );
        claim.addStatusHistory(history);
        historyRepository.save(history);

        // Update claim status
        claim.setStatus(toStatus);
        Claim updatedClaim = claimRepository.save(claim);

        // Record audit
        auditService.logAction(
                "Claim",
                claim.getId(),
                "STATUS_TRANSITION",
                String.format("Transitioned status from %s to %s. Reason: %s", fromStatus, toStatus, reason)
        );

        return updatedClaim;
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getStatusHistory(Long claimId) {
        if (!claimRepository.existsById(claimId)) {
            throw new ResourceNotFoundException("Claim not found with ID: " + claimId);
        }

        return historyRepository.findByClaimIdOrderByCreatedAtDesc(claimId).stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    private StatusHistoryResponse mapToHistoryResponse(ClaimStatusHistory history) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getClaim().getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedByUserId(),
                history.getChangedByUsername(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
