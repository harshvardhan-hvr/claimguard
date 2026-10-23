package com.claimguard.dto.response;

import java.util.List;

public class ClaimDetailResponse extends ClaimResponse {
    private RiskAssessmentResponse latestRiskAssessment;
    private List<StatusHistoryResponse> statusHistory;

    public ClaimDetailResponse() {
    }

    public RiskAssessmentResponse getLatestRiskAssessment() {
        return latestRiskAssessment;
    }

    public void setLatestRiskAssessment(RiskAssessmentResponse latestRiskAssessment) {
        this.latestRiskAssessment = latestRiskAssessment;
    }

    public List<StatusHistoryResponse> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusHistoryResponse> statusHistory) {
        this.statusHistory = statusHistory;
    }
}
