package com.claimguard.dto.response;

import java.util.Map;

public class RiskDistributionResponse {
    private Map<String, Long> riskDistribution;

    public RiskDistributionResponse() {
    }

    public RiskDistributionResponse(Map<String, Long> riskDistribution) {
        this.riskDistribution = riskDistribution;
    }

    public Map<String, Long> getRiskDistribution() {
        return riskDistribution;
    }

    public void setRiskDistribution(Map<String, Long> riskDistribution) {
        this.riskDistribution = riskDistribution;
    }
}
