package com.claimguard.service.workflow;

import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.Role;
import com.claimguard.exception.InvalidTransitionException;
import com.claimguard.exception.UnauthorizedActionException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StatusTransitionValidator {

    private static final Map<ClaimStatus, Set<ClaimStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ClaimStatus.class);

    static {
        // From SUBMITTED
        ALLOWED_TRANSITIONS.put(ClaimStatus.SUBMITTED, EnumSet.of(
                ClaimStatus.UNDER_REVIEW,
                ClaimStatus.FLAGGED,
                ClaimStatus.CLEARED
        ));

        // From UNDER_REVIEW
        ALLOWED_TRANSITIONS.put(ClaimStatus.UNDER_REVIEW, EnumSet.of(
                ClaimStatus.FLAGGED,
                ClaimStatus.CLEARED,
                ClaimStatus.CONFIRMED_FRAUD
        ));

        // From FLAGGED
        ALLOWED_TRANSITIONS.put(ClaimStatus.FLAGGED, EnumSet.of(
                ClaimStatus.UNDER_REVIEW,
                ClaimStatus.CLEARED,
                ClaimStatus.CONFIRMED_FRAUD
        ));

        // Terminal states can only be reopened to UNDER_REVIEW by ADMIN
        ALLOWED_TRANSITIONS.put(ClaimStatus.CLEARED, EnumSet.of(
                ClaimStatus.UNDER_REVIEW
        ));

        ALLOWED_TRANSITIONS.put(ClaimStatus.CONFIRMED_FRAUD, EnumSet.of(
                ClaimStatus.UNDER_REVIEW
        ));
    }

    public void validateTransition(ClaimStatus fromStatus, ClaimStatus toStatus, Role userRole, String reason) {
        if (fromStatus == null || toStatus == null) {
            throw new InvalidTransitionException("Source and destination statuses cannot be null.");
        }

        if (fromStatus == toStatus) {
            throw new InvalidTransitionException(String.format("Claim is already in '%s' status.", fromStatus));
        }

        if (reason == null || reason.trim().length() < 5) {
            throw new InvalidTransitionException("A mandatory justification reason (at least 5 characters) is required for status transitions.");
        }

        Set<ClaimStatus> allowedTargets = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowedTargets == null || !allowedTargets.contains(toStatus)) {
            throw new InvalidTransitionException(String.format(
                    "Invalid status transition from '%s' to '%s'. Allowed transitions from '%s' are: %s.",
                    fromStatus, toStatus, fromStatus, allowedTargets != null ? allowedTargets : "None"
            ));
        }

        // Final decisions (CLEARED, CONFIRMED_FRAUD) require CLAIMS_REVIEWER or ADMIN
        if (toStatus == ClaimStatus.CLEARED || toStatus == ClaimStatus.CONFIRMED_FRAUD) {
            if (userRole != Role.ADMIN && userRole != Role.CLAIMS_REVIEWER) {
                throw new UnauthorizedActionException("Only authorized Claims Reviewers or Administrators can mark a claim as CLEARED or CONFIRMED_FRAUD.");
            }
        }

        // Reopening terminal states requires ADMIN
        if ((fromStatus == ClaimStatus.CLEARED || fromStatus == ClaimStatus.CONFIRMED_FRAUD) && userRole != Role.ADMIN) {
            throw new UnauthorizedActionException("Only administrators can reopen a previously CLEARED or CONFIRMED_FRAUD claim.");
        }
    }
}
