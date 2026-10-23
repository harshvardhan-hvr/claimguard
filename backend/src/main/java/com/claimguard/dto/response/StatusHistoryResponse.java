package com.claimguard.dto.response;

import com.claimguard.enums.ClaimStatus;

import java.time.LocalDateTime;

public class StatusHistoryResponse {
    private Long id;
    private Long claimId;
    private ClaimStatus fromStatus;
    private ClaimStatus toStatus;
    private Long changedByUserId;
    private String changedByUsername;
    private String reason;
    private LocalDateTime createdAt;

    public StatusHistoryResponse() {
    }

    public StatusHistoryResponse(Long id, Long claimId, ClaimStatus fromStatus, ClaimStatus toStatus,
                                 Long changedByUserId, String changedByUsername, String reason,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.claimId = claimId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByUserId = changedByUserId;
        this.changedByUsername = changedByUsername;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public ClaimStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ClaimStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ClaimStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ClaimStatus toStatus) {
        this.toStatus = toStatus;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getChangedByUsername() {
        return changedByUsername;
    }

    public void setChangedByUsername(String changedByUsername) {
        this.changedByUsername = changedByUsername;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
