package com.claimguard.service.risk;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.service.risk.rules.DuplicateClaimRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateClaimRuleTest {

    private RiskEngineConfig config;
    private DuplicateClaimRule rule;

    @BeforeEach
    void setUp() {
        config = new RiskEngineConfig();
        rule = new DuplicateClaimRule(config);
    }

    @Test
    @DisplayName("Should trigger when claimant submits duplicate claim with identical amount and category within 30 days")
    void shouldTriggerOnDuplicateClaim() {
        LocalDate today = LocalDate.now();
        Claim existing = new Claim("CLM-PRIOR-01", "CLMT-DUPLICATE", BenefitCategory.HOUSING,
                new BigDecimal("1500.00"), today.minusDays(5), "First lease claim");
        existing.setId(101L);

        Claim newClaim = new Claim("CLM-NEW-02", "CLMT-DUPLICATE", BenefitCategory.HOUSING,
                new BigDecimal("1500.00"), today, "Second lease claim");
        newClaim.setId(102L);

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(existing));
        RuleEvaluationResult result = rule.evaluate(newClaim, context);

        assertTrue(result.isTriggered());
        assertEquals(40, result.getScoreContribution());
        assertTrue(result.getExplanation().contains("Potential duplicate detected"));
        assertTrue(result.getExplanation().contains("CLM-PRIOR-01"));
        assertTrue(result.getExplanation().contains("+40 pts"));
    }

    @Test
    @DisplayName("Should pass when amount is different even for same claimant and category")
    void shouldPassWhenAmountDiffers() {
        LocalDate today = LocalDate.now();
        Claim existing = new Claim("CLM-PRIOR-01", "CLMT-DUPLICATE", BenefitCategory.HOUSING,
                new BigDecimal("1200.00"), today.minusDays(5), "First lease claim");

        Claim newClaim = new Claim("CLM-NEW-02", "CLMT-DUPLICATE", BenefitCategory.HOUSING,
                new BigDecimal("1500.00"), today, "Second lease claim");

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(existing));
        RuleEvaluationResult result = rule.evaluate(newClaim, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }

    @Test
    @DisplayName("Should pass when duplicate is older than the 30-day inspection window")
    void shouldPassWhenDuplicateOlderThanWindow() {
        LocalDate today = LocalDate.now();
        Claim existing = new Claim("CLM-OLD-01", "CLMT-DUPLICATE", BenefitCategory.HOUSING,
                new BigDecimal("1500.00"), today.minusDays(45), "Old lease claim");

        Claim newClaim = new Claim("CLM-NEW-02", "CLMT-DUPLICATE", BenefitCategory.HOUSING,
                new BigDecimal("1500.00"), today, "Second lease claim");

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(existing));
        RuleEvaluationResult result = rule.evaluate(newClaim, context);

        assertFalse(result.isTriggered());
    }

    @Test
    @DisplayName("Should pass when there are no historical claims for the claimant")
    void shouldPassWithEmptyHistory() {
        Claim newClaim = new Claim("CLM-NEW-01", "CLMT-NEWBIE", BenefitCategory.HOUSING,
                new BigDecimal("1500.00"), LocalDate.now(), "First claim ever");

        RiskEvaluationContext context = new RiskEvaluationContext(List.of());
        RuleEvaluationResult result = rule.evaluate(newClaim, context);

        assertFalse(result.isTriggered());
    }
}
