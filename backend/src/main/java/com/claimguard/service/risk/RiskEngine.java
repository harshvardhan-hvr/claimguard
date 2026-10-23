package com.claimguard.service.risk;

import com.claimguard.entity.Claim;

public interface RiskEngine {
    RiskEvaluationResult evaluate(Claim claim, RiskEvaluationContext context);
}
