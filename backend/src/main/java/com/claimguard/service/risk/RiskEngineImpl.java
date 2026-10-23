package com.claimguard.service.risk;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.enums.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RiskEngineImpl implements RiskEngine {

    private static final Logger log = LoggerFactory.getLogger(RiskEngineImpl.class);

    private final List<RiskRule> rules;
    private final RiskEngineConfig config;

    public RiskEngineImpl(List<RiskRule> rules, RiskEngineConfig config) {
        // Sort rules by priority
        List<RiskRule> sortedRules = new ArrayList<>(rules != null ? rules : List.of());
        sortedRules.sort(Comparator.comparingInt(RiskRule::getPriority));
        this.rules = sortedRules;
        this.config = config;
    }

    @Override
    public RiskEvaluationResult evaluate(Claim claim, RiskEvaluationContext context) {
        if (claim == null) {
            throw new IllegalArgumentException("Claim cannot be null for risk evaluation.");
        }

        List<RuleEvaluationResult> ruleResults = new ArrayList<>();
        int accumulatedScore = 0;
        int triggeredCount = 0;

        for (RiskRule rule : rules) {
            try {
                RuleEvaluationResult result = rule.evaluate(claim, context);
                ruleResults.add(result);
                if (result.isTriggered()) {
                    accumulatedScore += result.getScoreContribution();
                    triggeredCount++;
                }
            } catch (Exception e) {
                log.error("Error executing risk rule: {}", rule.getRuleCode(), e);
                ruleResults.add(RuleEvaluationResult.pass(
                        rule.getRuleCode(),
                        rule.getRuleName(),
                        "Rule execution error: " + e.getMessage()
                ));
            }
        }

        // Cap final risk score at 100
        int finalScore = Math.min(100, Math.max(0, accumulatedScore));

        // Determine risk level based on thresholds
        RiskLevel riskLevel;
        if (finalScore <= config.getScoring().getLowMax()) {
            riskLevel = RiskLevel.LOW;
        } else if (finalScore <= config.getScoring().getMediumMax()) {
            riskLevel = RiskLevel.MEDIUM;
        } else {
            riskLevel = RiskLevel.HIGH;
        }

        String summary = String.format(
                "Risk evaluation complete: Assigned score %d/100 (%s). %d of %d rules triggered.",
                finalScore,
                riskLevel.name(),
                triggeredCount,
                rules.size()
        );

        return new RiskEvaluationResult(finalScore, riskLevel, ruleResults, summary);
    }
}
