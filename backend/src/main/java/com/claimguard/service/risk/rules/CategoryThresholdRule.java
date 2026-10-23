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
import java.util.Locale;

@Component
@Order(10)
public class CategoryThresholdRule implements RiskRule {

    public static final String RULE_CODE = "RULE_CATEGORY_THRESHOLD";
    public static final String RULE_NAME = "Category Threshold Exceeded";

    private final RiskEngineConfig config;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

    public CategoryThresholdRule(RiskEngineConfig config) {
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
        return "Checks if the submitted claim amount exceeds the policy threshold defined for its benefit category.";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, RiskEvaluationContext context) {
        if (claim == null || claim.getAmount() == null || claim.getCategory() == null) {
            return RuleEvaluationResult.pass(RULE_CODE, RULE_NAME, "Missing claim details for threshold evaluation.");
        }

        BigDecimal threshold = config.getThresholdForCategory(claim.getCategory());
        if (claim.getAmount().compareTo(threshold) > 0) {
            BigDecimal difference = claim.getAmount().subtract(threshold);
            int score = config.getScoring().getCategoryThresholdScore();
            String explanation = String.format(
                    "Claim amount %s exceeds the %s threshold of %s by %s (+%d pts).",
                    currencyFormatter.format(claim.getAmount()),
                    claim.getCategory().name(),
                    currencyFormatter.format(threshold),
                    currencyFormatter.format(difference),
                    score
            );
            return RuleEvaluationResult.triggered(RULE_CODE, RULE_NAME, score, explanation);
        }

        return RuleEvaluationResult.pass(
                RULE_CODE,
                RULE_NAME,
                String.format("Claim amount %s is within the %s category threshold of %s.",
                        currencyFormatter.format(claim.getAmount()),
                        claim.getCategory().name(),
                        currencyFormatter.format(threshold))
        );
    }
}
