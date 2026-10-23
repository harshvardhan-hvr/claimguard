package com.claimguard.service.risk;

import com.claimguard.entity.Claim;

import java.util.*;

public class RiskEvaluationContext {
    private final List<Claim> historicalClaims;
    private final Map<String, Object> metadata;

    public RiskEvaluationContext(List<Claim> historicalClaims) {
        this.historicalClaims = historicalClaims != null ? Collections.unmodifiableList(historicalClaims) : Collections.emptyList();
        this.metadata = new HashMap<>();
    }

    public List<Claim> getHistoricalClaims() {
        return historicalClaims;
    }

    public void putMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
}
