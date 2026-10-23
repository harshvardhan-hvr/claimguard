package com.claimguard.service.risk;

import com.claimguard.config.RiskEngineConfig;
import com.claimguard.entity.Claim;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.enums.RiskLevel;
import com.claimguard.service.risk.rules.CategoryThresholdRule;
import com.claimguard.service.risk.rules.ClaimantVelocityRule;
import com.claimguard.service.risk.rules.DuplicateClaimRule;
import com.claimguard.service.risk.rules.UnusualFrequencyRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskEngineTest {

    private RiskEngine riskEngine;
    private RiskEngineConfig config;

    @BeforeEach
    void setUp() {
        config = new RiskEngineConfig();
        List<RiskRule> rules = List.of(
                new CategoryThresholdRule(config),
                new DuplicateClaimRule(config),
                new ClaimantVelocityRule(config),
                new UnusualFrequencyRule(config)
        );
        riskEngine = new RiskEngineImpl(rules, config);
    }

    @Test
    @DisplayName("Clean claim with no triggers should score 0 and classify as LOW risk")
    void shouldClassifyCleanClaimAsLowRisk() {
        Claim claim = new Claim("CLM-CLEAN", "CLMT-100", BenefitCategory.HEALTHCARE,
                new BigDecimal("500.00"), LocalDate.now(), "Clean claim");
        RiskEvaluationContext context = new RiskEvaluationContext(List.of());

        RiskEvaluationResult result = riskEngine.evaluate(claim, context);

        assertEquals(0, result.getTotalScore());
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertEquals(4, result.getRuleResults().size());
        assertTrue(result.getRuleResults().stream().noneMatch(RuleEvaluationResult::isTriggered));
    }

    @Test
    @DisplayName("Single threshold trigger (35 pts) should classify as MEDIUM risk")
    void shouldClassifySingleThresholdTriggerAsMediumRisk() {
        Claim claim = new Claim("CLM-MED", "CLMT-101", BenefitCategory.HEALTHCARE,
                new BigDecimal("6000.00"), LocalDate.now(), "High amount healthcare");
        RiskEvaluationContext context = new RiskEvaluationContext(List.of());

        RiskEvaluationResult result = riskEngine.evaluate(claim, context);

        assertEquals(35, result.getTotalScore());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
    }

    @Test
    @DisplayName("Duplicate claim (40) + Threshold (35) = 75 pts should classify as HIGH risk")
    void shouldClassifyHighRiskWhenMultipleRulesTrigger() {
        LocalDate today = LocalDate.now();
        Claim prior = new Claim("CLM-PRIOR", "CLMT-102", BenefitCategory.HEALTHCARE,
                new BigDecimal("6000.00"), today.minusDays(5), "Prior claim");
        prior.setId(50L);

        Claim current = new Claim("CLM-NEW", "CLMT-102", BenefitCategory.HEALTHCARE,
                new BigDecimal("6000.00"), today, "Duplicate high amount claim");
        current.setId(51L);

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(prior));
        RiskEvaluationResult result = riskEngine.evaluate(current, context);

        // Threshold (35) + Duplicate (40) = 75
        assertEquals(75, result.getTotalScore());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
    }

    @Test
    @DisplayName("Cumulative score exceeding 100 should be capped strictly at 100")
    void shouldCapScoreAt100WhenExceedingMax() {
        LocalDate today = LocalDate.now();
        // Create 2 prior claims so velocity (> 2) triggers
        Claim prior1 = new Claim("CLM-P1", "CLMT-103", BenefitCategory.HEALTHCARE,
                new BigDecimal("6000.00"), today.minusDays(1), "P1");
        prior1.setId(61L);
        Claim prior2 = new Claim("CLM-P2", "CLMT-103", BenefitCategory.HEALTHCARE,
                new BigDecimal("200.00"), today.minusDays(10), "P2");
        prior2.setId(62L);

        // Current claim matches prior1 amount (Duplicate: 40), exceeds threshold (35),
        // 3rd claim in 30 days (Velocity: 30), and within 1 day of prior1 (Rapid: 25)
        // Total raw: 40 + 35 + 30 + 25 = 130 -> Capped at 100
        Claim current = new Claim("CLM-P3", "CLMT-103", BenefitCategory.HEALTHCARE,
                new BigDecimal("6000.00"), today, "Critical anomaly");
        current.setId(63L);

        RiskEvaluationContext context = new RiskEvaluationContext(List.of(prior1, prior2));
        RiskEvaluationResult result = riskEngine.evaluate(current, context);

        assertEquals(100, result.getTotalScore());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
    }
}
