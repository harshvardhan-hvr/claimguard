package com.claimguard.service.risk.rules;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.service.risk.RiskEvaluationContext;
import com.claimguard.service.risk.RiskRule;
import com.claimguard.service.risk.RuleEvaluationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
@Order(30)
public class ClaimantVelocityRule implements RiskRule {

    public static final String RULE_CODE = "RULE_CLAIMANT_VELOCITY";
    public static final String RULE_NAME = "High Claimant Submission Frequency";

    private final RiskEngineConfig config;

    public ClaimantVelocityRule(RiskEngineConfig config) {
        this.config = config;
    }

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }

    @Override
    public String getDescription() {
        return "Flags claimants who submit an unusually high number of benefit claims within a rolling 30-day window.";
    }

    @Override
    public int getPriority() {
        return 30;
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, RiskEvaluationContext context) {
        if (claim == null || claim.getClaimantId() == null || claim.getSubmissionDate() == null) {
            return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "Missing claimant submission date information.");
        }

        if (context == null || context.getHistoricalClaims() == null || context.getHistoricalClaims().isEmpty()) {
            return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "Single claim on record for claimant; velocity normal.");
        }

        int lookbackDays = config.getVelocity().getLookbackDays();
        int maxAllowed = config.getVelocity().getMaxAllowedClaims();
        LocalDate cutoffDate = claim.getSubmissionDate().minusDays(lookbackDays);

        long recentClaimsCount = context.getHistoricalClaims().stream()
                .filter(other -> other != null
                        && !Objects.equals(other.getId(), claim.getId())
                        && !Objects.equals(other.getClaimReference(), claim.getClaimReference())
                        && other.getSubmissionDate() != null
                        && !other.getSubmissionDate().isBefore(cutoffDate)
                        && !other.getSubmissionDate().isAfter(claim.getSubmissionDate()))
                .count();

        // Including current claim, total claims in window
        long totalInWindow = recentClaimsCount + 1;

        if (totalInWindow > maxAllowed) {
            int score = config.getScoring().getClaimantVelocityScore();
            String explanation = String.format(
                    "High submission velocity: claimant '%s' submitted %d claims in a %d-day window (policy threshold: %d) (+%d pts).",
                    claim.getClaimantId(),
                    totalInWindow,
                    lookbackDays,
                    maxAllowed,
                    score
            );
            return RuleEvaluationResult.triggered(RULE_CODE, RULE_NAME, score, explanation);
        }

        return RuleEvaluationResult.pass(
                RULE_CODE,
                RULE_NAME,
                String.format("Claimant velocity is normal: %d claim(s) in %d-day window (allowance: %d).",
                        totalInWindow, lookbackDays, maxAllowed)
        );
    }
}
