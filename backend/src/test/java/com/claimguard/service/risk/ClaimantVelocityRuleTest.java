package com.claimguard.service.risk;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.service.risk.rules.ClaimantVelocityRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimantVelocityRuleTest {

    private RiskEngineConfig config;
    private ClaimantVelocityRule rule;

    @BeforeEach
    void setUp() {
        config = new RiskEngineConfig();
        rule = new ClaimantVelocityRule(config);
    }

    @Test
    @DisplayName("Should trigger when claimant submits 3 claims within 30 days (max allowed is 2)")
    void shouldTriggerWhenClaimantExceedsVelocityThreshold() {
        LocalDate today = LocalDate.now();
        Claim claim1 = new Claim("CLM-V-1", "CLMT-SPEED", BenefitCategory.HEALTHCARE, new BigDecimal("200.00"), today.minusDays(20), "Visit 1");
        claim1.setId(1L);
        Claim claim2 = new Claim("CLM-V-2", "CLMT-SPEED", BenefitCategory.HEALTHCARE, new BigDecimal("300.00"), today.minusDays(10), "Visit 2");
        claim2.setId(2L);

        Claim currentClaim = new Claim("CLM-V-3", "CLMT-SPEED", BenefitCategory.HEALTHCARE, new BigDecimal("400.00"), today, "Visit 3");
        currentClaim.setId(3L);

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(claim1, claim2));
        RuleEvaluationResult result = rule.evaluate(currentClaim, context);

        assertTrue(result.isTriggered());
        assertEquals(30, result.getScoreContribution());
        assertTrue(result.getExplanation().contains("High submission velocity"));
        assertTrue(result.getExplanation().contains("+30 pts"));
    }

    @Test
    @DisplayName("Should pass when claimant has only 2 claims in 30 days (within limit of 2)")
    void shouldPassWhenVelocityWithinAllowedLimit() {
        LocalDate today = LocalDate.now();
        Claim claim1 = new Claim("CLM-V-1", "CLMT-SLOW", BenefitCategory.HEALTHCARE, new BigDecimal("200.00"), today.minusDays(20), "Visit 1");
        claim1.setId(1L);

        Claim currentClaim = new Claim("CLM-V-2", "CLMT-SLOW", BenefitCategory.HEALTHCARE, new BigDecimal("400.00"), today, "Visit 2");
        currentClaim.setId(2L);

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(claim1));
        RuleEvaluationResult result = rule.evaluate(currentClaim, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }
}
