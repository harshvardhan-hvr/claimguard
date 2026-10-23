package com.claimguard.service.risk;

import com.claimguard.enums.RiskLevel;

import java.util.Collections;
import java.util.List;

public class RiskEvaluationResult {
    private final int totalScore;
    private final RiskLevel riskLevel;
    private final List<RuleEvaluationResult> ruleResults;
    private final String summary;

    public RiskEvaluationResult(int totalScore, RiskLevel riskLevel, List<RuleEvaluationResult> ruleResults, String summary) {
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.ruleResults = ruleResults != null ? Collections.unmodifiableList(ruleResults) : Collections.emptyList();
        this.summary = summary;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public List<RuleEvaluationResult> getRuleResults() {
        return ruleResults;
    }

    public String getSummary() {
        return summary;
    }
}
