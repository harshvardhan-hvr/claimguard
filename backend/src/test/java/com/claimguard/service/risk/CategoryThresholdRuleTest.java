package com.claimguard.service.risk;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.service.risk.rules.CategoryThresholdRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CategoryThresholdRuleTest {

    private RiskEngineConfig config;
    private CategoryThresholdRule rule;

    @BeforeEach
    void setUp() {
        config = new RiskEngineConfig();
        rule = new CategoryThresholdRule(config);
    }

    @Test
    @DisplayName("Should trigger when healthcare claim exceeds $5000 threshold")
    void shouldTriggerWhenHealthcareAmountExceedsThreshold() {
        Claim claim = new Claim("CLM-TEST-01", "CLMT-001", BenefitCategory.HEALTHCARE,
                new BigDecimal("6500.00"), LocalDate.now(), "Surgery claim");
        RiskEvaluationContext context = new RiskEvaluationContext(Collections.emptyList());

        RuleEvaluationResult result = rule.evaluate(claim, context);

        assertTrue(result.isTriggered());
        assertEquals(35, result.getScoreContribution());
        assertTrue(result.getExplanation().contains("exceeds the HEALTHCARE threshold"));
        assertTrue(result.getExplanation().contains("+35 pts"));
    }

    @Test
    @DisplayName("Should pass when healthcare claim is below $5000 threshold")
    void shouldPassWhenHealthcareAmountBelowThreshold() {
        Claim claim = new Claim("CLM-TEST-02", "CLMT-001", BenefitCategory.HEALTHCARE,
                new BigDecimal("3500.00"), LocalDate.now(), "Routine dental checkup");
        RiskEvaluationContext context = new RiskEvaluationContext(Collections.emptyList());

        RuleEvaluationResult result = rule.evaluate(claim, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
        assertTrue(result.getExplanation().contains("is within the HEALTHCARE category threshold"));
    }

    @Test
    @DisplayName("Should pass when claim amount is exactly equal to threshold")
    void shouldPassWhenAmountEqualsThreshold() {
        Claim claim = new Claim("CLM-TEST-03", "CLMT-001", BenefitCategory.HEALTHCARE,
                new BigDecimal("5000.00"), LocalDate.now(), "Medical equipment");
        RiskEvaluationContext context = new RiskEvaluationContext(Collections.emptyList());

        RuleEvaluationResult result = rule.evaluate(claim, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }

    @Test
    @DisplayName("Should safely handle null claim or null amount without throwing exception")
    void shouldHandleNullDataSafely() {
        RiskEvaluationContext context = new RiskEvaluationContext(Collections.emptyList());

        RuleEvaluationResult nullClaimResult = rule.evaluate(null, context);
        assertFalse(nullClaimResult.isTriggered());

        Claim claimWithNullAmount = new Claim("CLM-TEST-04", "CLMT-001", BenefitCategory.HOUSING,
                null, LocalDate.now(), "Test");
        RuleEvaluationResult nullAmountResult = rule.evaluate(claimWithNullAmount, context);
        assertFalse(nullAmountResult.isTriggered());
    }
}
