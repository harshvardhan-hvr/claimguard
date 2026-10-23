package com.claimguard.service.risk;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.service.risk.rules.UnusualFrequencyRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnusualFrequencyRuleTest {

    private RiskEngineConfig config;
    private UnusualFrequencyRule rule;

    @BeforeEach
    void setUp() {
        config = new RiskEngineConfig();
        rule = new UnusualFrequencyRule(config);
    }

    @Test
    @DisplayName("Should trigger when another claim is submitted within 48 hours (1 day apart)")
    void shouldTriggerWhenClaimWithin48Hours() {
        LocalDate today = LocalDate.now();
        Claim yesterday = new Claim("CLM-F-1", "CLMT-BURST", BenefitCategory.HOUSING, new BigDecimal("800.00"), today.minusDays(1), "Yesterday claim");
        yesterday.setId(10L);

        Claim todayClaim = new Claim("CLM-F-2", "CLMT-BURST", BenefitCategory.CHILDCARE, new BigDecimal("500.00"), today, "Today claim");
        todayClaim.setId(11L);

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(yesterday));
        RuleEvaluationResult result = rule.evaluate(todayClaim, context);

        assertTrue(result.isTriggered());
        assertEquals(25, result.getScoreContribution());
        assertTrue(result.getExplanation().contains("Rapid submission alert"));
        assertTrue(result.getExplanation().contains("+25 pts"));
    }

    @Test
    @DisplayName("Should pass when submissions are spaced more than 2 days apart")
    void shouldPassWhenSubmissionsAreWellSpaced() {
        LocalDate today = LocalDate.now();
        Claim prior = new Claim("CLM-F-1", "CLMT-NORMAL", BenefitCategory.HOUSING, new BigDecimal("800.00"), today.minusDays(10), "Prior claim");

        Claim current = new Claim("CLM-F-2", "CLMT-NORMAL", BenefitCategory.CHILDCARE, new BigDecimal("500.00"), today, "Current claim");

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(prior));
        RuleEvaluationResult result = rule.evaluate(current, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }
}
