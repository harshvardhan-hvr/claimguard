package com.claimguard.dto.response;

import com.claimguard.enums.RiskLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RiskAssessmentResponse {
    private Long id;
    private Long claimId;
    private int totalScore;
    private RiskLevel riskLevel;
    private LocalDateTime evaluatedAt;
    private String evaluatorNote;
    private List<RiskRuleResultResponse> ruleResults = new ArrayList<>();

    public RiskAssessmentResponse() {
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

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public String getEvaluatorNote() {
        return evaluatorNote;
    }

    public void setEvaluatorNote(String evaluatorNote) {
        this.evaluatorNote = evaluatorNote;
    }

    public List<RiskRuleResultResponse> getRuleResults() {
        return ruleResults;
    }

    public void setRuleResults(List<RiskRuleResultResponse> ruleResults) {
        this.ruleResults = ruleResults;
    }
}
