package com.claimguard.service.risk;

import com.claimguard.entity.Claim;

public interface RiskRule {
    String getRuleCode();
    String getRuleName();
    String getDescription();
    int getPriority();
    RuleEvaluationResult evaluate(Claim claim, RiskEvaluationContext context);
}
