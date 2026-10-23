package com.claimguard.dto.response;

public class RiskRuleResultResponse {
    private Long id;
    private String ruleName;
    private String ruleCode;
    private int scoreContribution;
    private boolean triggered;
    private String explanation;

    public RiskRuleResultResponse() {
    }

    public RiskRuleResultResponse(Long id, String ruleName, String ruleCode, int scoreContribution, boolean triggered, String explanation) {
        this.id = id;
        this.ruleName = ruleName;
        this.ruleCode = ruleCode;
        this.scoreContribution = scoreContribution;
        this.triggered = triggered;
        this.explanation = explanation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public int getScoreContribution() {
        return scoreContribution;
    }

    public void setScoreContribution(int scoreContribution) {
        this.scoreContribution = scoreContribution;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
