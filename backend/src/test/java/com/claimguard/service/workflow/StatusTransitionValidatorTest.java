package com.claimguard.service.workflow;

import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.Role;
import com.claimguard.exception.InvalidTransitionException;
import com.claimguard.exception.UnauthorizedActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTransitionValidatorTest {

    private StatusTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StatusTransitionValidator();
    }

    @Test
    @DisplayName("Valid transition from SUBMITTED to UNDER_REVIEW should pass")
    void shouldAllowTransitionFromSubmittedToUnderReview() {
        assertDoesNotThrow(() ->
                validator.validateTransition(
                        ClaimStatus.SUBMITTED,
                        ClaimStatus.UNDER_REVIEW,
                        Role.CLAIMS_REVIEWER,
                        "Assigned to specialist for receipt audit"
                )
        );
    }

    @Test
    @DisplayName("Valid transition from UNDER_REVIEW to CLEARED by Reviewer should pass")
    void shouldAllowClearingClaimByReviewer() {
        assertDoesNotThrow(() ->
                validator.validateTransition(
                        ClaimStatus.UNDER_REVIEW,
                        ClaimStatus.CLEARED,
                        Role.CLAIMS_REVIEWER,
                        "Invoices independently verified with clinic"
                )
        );
    }

    @Test
    @DisplayName("Direct transition from SUBMITTED to CONFIRMED_FRAUD without review must be rejected")
    void shouldRejectDirectFraudConfirmationFromSubmitted() {
        InvalidTransitionException ex = assertThrows(InvalidTransitionException.class, () ->
                validator.validateTransition(
                        ClaimStatus.SUBMITTED,
                        ClaimStatus.CONFIRMED_FRAUD,
                        Role.CLAIMS_REVIEWER,
                        "Attempting immediate fraud marking"
                )
        );
        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }

    @Test
    @DisplayName("Transitioning to the exact same status must be rejected")
    void shouldRejectTransitionToSameStatus() {
        InvalidTransitionException ex = assertThrows(InvalidTransitionException.class, () ->
                validator.validateTransition(
                        ClaimStatus.UNDER_REVIEW,
                        ClaimStatus.UNDER_REVIEW,
                        Role.CLAIMS_REVIEWER,
                        "No change in status attempted"
                )
        );
        assertTrue(ex.getMessage().contains("already in 'UNDER_REVIEW' status"));
    }

    @Test
    @DisplayName("Transition without adequate justification reason (< 5 chars) must be rejected")
    void shouldRejectTransitionWithEmptyOrShortReason() {
        InvalidTransitionException ex = assertThrows(InvalidTransitionException.class, () ->
                validator.validateTransition(
                        ClaimStatus.SUBMITTED,
                        ClaimStatus.UNDER_REVIEW,
                        Role.CLAIMS_REVIEWER,
                        "ok"
                )
        );
        assertTrue(ex.getMessage().contains("mandatory justification reason"));
    }

    @Test
    @DisplayName("Non-admin user cannot reopen a CONFIRMED_FRAUD claim")
    void shouldRejectReopeningFraudClaimByNonAdmin() {
        UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class, () ->
                validator.validateTransition(
                        ClaimStatus.CONFIRMED_FRAUD,
                        ClaimStatus.UNDER_REVIEW,
                        Role.CLAIMS_REVIEWER,
                        "Attempting to reopen fraud case"
                )
        );
        assertTrue(ex.getMessage().contains("Only administrators can reopen"));
    }

    @Test
    @DisplayName("Admin user can reopen a CONFIRMED_FRAUD claim with valid reason")
    void shouldAllowReopeningFraudClaimByAdmin() {
        assertDoesNotThrow(() ->
                validator.validateTransition(
                        ClaimStatus.CONFIRMED_FRAUD,
                        ClaimStatus.UNDER_REVIEW,
                        Role.ADMIN,
                        "Reopening fraud case following official appeal document submission"
                )
        );
    }
}
