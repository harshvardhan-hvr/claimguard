package com.claimguard.dto.request;

import com.claimguard.enums.ClaimStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StatusTransitionRequest {

    @NotNull(message = "Target status is required")
    private ClaimStatus toStatus;

    @NotBlank(message = "A justification reason is required for status transitions")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    public StatusTransitionRequest() {
    }

    public StatusTransitionRequest(ClaimStatus toStatus, String reason) {
        this.toStatus = toStatus;
        this.reason = reason;
    }

    public ClaimStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ClaimStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
