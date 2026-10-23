package com.claimguard.service.risk.rules;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.service.risk.RiskEvaluationContext;
import com.claimguard.service.risk.RiskRule;
import com.claimguard.service.risk.RuleEvaluationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
@Order(20)
public class DuplicateClaimRule implements RiskRule {

    public static final String RULE_CODE = "RULE_DUPLICATE_CLAIM";
    public static final String RULE_NAME = "Potential Duplicate Claim Submission";

    private final RiskEngineConfig config;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

    public DuplicateClaimRule(RiskEngineConfig config) {
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
        return "Detects duplicate or near-duplicate claims submitted by the same claimant with matching amount and category.";
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, RiskEvaluationContext context) {
        if (claim == null || claim.getAmount() == null || claim.getClaimantId() == null) {
            return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "Missing claim information for duplicate check.");
        }

        if (context == null || context.getHistoricalClaims() == null || context.getHistoricalClaims().isEmpty()) {
            return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "No prior claims found for claimant " + claim.getClaimantId() + ".");
        }

        int windowDays = config.getVelocity().getLookbackDays();

        // Search for existing claim with exact amount and same category within window
        Optional<Claim> duplicateMatch = context.getHistoricalClaims().stream()
                .filter(other -> other != null
                        && !Objects.equals(other.getId(), claim.getId())
                        && !Objects.equals(other.getClaimReference(), claim.getClaimReference())
                        && other.getAmount() != null
                        && other.getAmount().compareTo(claim.getAmount()) == 0
                        && other.getCategory() == claim.getCategory()
                        && other.getSubmissionDate() != null
                        && claim.getSubmissionDate() != null
                        && Math.abs(ChronoUnit.DAYS.between(other.getSubmissionDate(), claim.getSubmissionDate())) <= windowDays)
                .findFirst();

        if (duplicateMatch.isPresent()) {
            Claim dup = duplicateMatch.get();
            int score = config.getScoring().getDuplicateClaimScore();
            long daysApart = Math.abs(ChronoUnit.DAYS.between(dup.getSubmissionDate(), claim.getSubmissionDate()));
            String explanation = String.format(
                    "Potential duplicate detected: identical %s amount of %s matches prior claim '%s' submitted %d day(s) apart on %s (+%d pts).",
                    claim.getCategory().name(),
                    currencyFormatter.format(claim.getAmount()),
                    dup.getClaimReference(),
                    daysApart,
                    dup.getSubmissionDate(),
                    score
            );
            return RuleEvaluationResult.triggered(RULE_CODE, RULE_NAME, score, explanation);
        }

        return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "No duplicate submissions detected for this claimant within the inspection window.");
    }
}
