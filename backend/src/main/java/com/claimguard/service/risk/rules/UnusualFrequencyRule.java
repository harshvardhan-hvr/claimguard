package com.claimguard.service.risk.rules;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.service.risk.RiskEvaluationContext;
import com.claimguard.service.risk.RiskRule;
import com.claimguard.service.risk.RuleEvaluationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Component
@Order(40)
public class UnusualFrequencyRule implements RiskRule {

    public static final String RULE_CODE = "RULE_RAPID_SUBMISSION";
    public static final String RULE_NAME = "Rapid Consecutive Submission Pattern";

    private final RiskEngineConfig config;

    public UnusualFrequencyRule(RiskEngineConfig config) {
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
        return "Flags submissions occurring within an abnormally short time interval (within 48 hours of another claim).";
    }

    @Override
    public int getPriority() {
        return 40;
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, RiskEvaluationContext context) {
        if (claim == null || claim.getSubmissionDate() == null || claim.getClaimantId() == null) {
            return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "Missing submission date information.");
        }

        if (context == null || context.getHistoricalClaims() == null || context.getHistoricalClaims().isEmpty()) {
            return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "No prior submissions to compare spacing.");
        }

        // Look for any other claim submitted within 2 days (<= 48 hours / 2 days)
        Optional<Claim> rapidSuccessor = context.getHistoricalClaims().stream()
                .filter(other -> other != null
                        && !Objects.equals(other.getId(), claim.getId())
                        && !Objects.equals(other.getClaimReference(), claim.getClaimReference())
                        && other.getSubmissionDate() != null)
                .filter(other -> Math.abs(ChronoUnit.DAYS.between(other.getSubmissionDate(), claim.getSubmissionDate())) <= 2)
                .findFirst();

        if (rapidSuccessor.isPresent()) {
            Claim other = rapidSuccessor.get();
            long days = Math.abs(ChronoUnit.DAYS.between(other.getSubmissionDate(), claim.getSubmissionDate()));
            int score = config.getScoring().getRapidSubmissionScore();
            String explanation = String.format(
                    "Rapid submission alert: claimant submitted another claim '%s' %d day(s) apart on %s (+%d pts).",
                    other.getClaimReference(),
                    days,
                    other.getSubmissionDate(),
                    score
            );
            return RuleEvaluationResult.triggered(RULE_CODE, RULE_NAME, score, explanation);
        }

        return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "Submission timing shows normal separation between claims.");
    }
}
