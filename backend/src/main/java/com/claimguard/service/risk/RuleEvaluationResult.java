package com.claimguard.service.risk;

public class RuleEvaluationResult {
    private final String ruleCode;
    private final String ruleName;
    private final boolean triggered;
    private final int scoreContribution;
    private final String explanation;

    public RuleEvaluationResult(String ruleCode, String ruleName, boolean triggered,
                                int scoreContribution, String explanation) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.triggered = triggered;
        this.scoreContribution = scoreContribution;
        this.explanation = explanation;
    }

    public static RuleEvaluationResult pass(String ruleCode, String ruleName, String explanation) {
        return new RuleEvaluationResult(ruleCode, ruleName, false, 0, explanation);
    }

    public static RuleEvaluationResult triggered(String ruleCode, String ruleName,
                                                int scoreContribution, String explanation) {
        return new RuleEvaluationResult(ruleCode, ruleName, true, scoreContribution, explanation);
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public int getScoreContribution() {
        return scoreContribution;
    }

    public String getExplanation() {
        return explanation;
    }
}
