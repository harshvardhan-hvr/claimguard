package com.claimguard.dto.response;

import java.util.Map;

public class ClaimsByStatusResponse {
    private Map<String, Long> statusCounts;

    public ClaimsByStatusResponse() {
    }

    public ClaimsByStatusResponse(Map<String, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }
}
