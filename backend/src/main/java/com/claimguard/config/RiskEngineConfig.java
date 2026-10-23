package com.claimguard.config;

import com.claimguard.enums.BenefitCategory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "risk-engine")
public class RiskEngineConfig {

    private Map<BenefitCategory, BigDecimal> thresholds = new EnumMap<>(BenefitCategory.class);
    private Scoring scoring = new Scoring();
    private Velocity velocity = new Velocity();

    public static class Scoring {
        private int lowMax = 29;
        private int mediumMax = 69;
        private int categoryThresholdScore = 35;
        private int claimantVelocityScore = 30;
        private int duplicateClaimScore = 40;
        private int rapidSubmissionScore = 25;

        public int getLowMax() {
            return lowMax;
        }

        public void setLowMax(int lowMax) {
            this.lowMax = lowMax;
        }

        public int getMediumMax() {
            return mediumMax;
        }

        public void setMediumMax(int mediumMax) {
            this.mediumMax = mediumMax;
        }

        public int getCategoryThresholdScore() {
            return categoryThresholdScore;
        }

        public void setCategoryThresholdScore(int categoryThresholdScore) {
            this.categoryThresholdScore = categoryThresholdScore;
        }

        public int getClaimantVelocityScore() {
            return claimantVelocityScore;
        }

        public void setClaimantVelocityScore(int claimantVelocityScore) {
            this.claimantVelocityScore = claimantVelocityScore;
        }

        public int getDuplicateClaimScore() {
            return duplicateClaimScore;
        }

        public void setDuplicateClaimScore(int duplicateClaimScore) {
            this.duplicateClaimScore = duplicateClaimScore;
        }

        public int getRapidSubmissionScore() {
            return rapidSubmissionScore;
        }

        public void setRapidSubmissionScore(int rapidSubmissionScore) {
            this.rapidSubmissionScore = rapidSubmissionScore;
        }
    }

    public static class Velocity {
        private int lookbackDays = 30;
        private int maxAllowedClaims = 2;
        private int rapidSubmissionHours = 48;

        public int getLookbackDays() {
            return lookbackDays;
        }

        public void setLookbackDays(int lookbackDays) {
            this.lookbackDays = lookbackDays;
        }

        public int getMaxAllowedClaims() {
            return maxAllowedClaims;
        }

        public void setMaxAllowedClaims(int maxAllowedClaims) {
            this.maxAllowedClaims = maxAllowedClaims;
        }

        public int getRapidSubmissionHours() {
            return rapidSubmissionHours;
        }

        public void setRapidSubmissionHours(int rapidSubmissionHours) {
            this.rapidSubmissionHours = rapidSubmissionHours;
        }
    }

    public Map<BenefitCategory, BigDecimal> getThresholds() {
        return thresholds;
    }

    public void setThresholds(Map<BenefitCategory, BigDecimal> thresholds) {
        this.thresholds = thresholds;
    }

    public Scoring getScoring() {
        return scoring;
    }

    public void setScoring(Scoring scoring) {
        this.scoring = scoring;
    }

    public Velocity getVelocity() {
        return velocity;
    }

    public void setVelocity(Velocity velocity) {
        this.velocity = velocity;
    }

    public BigDecimal getThresholdForCategory(BenefitCategory category) {
        if (thresholds == null || !thresholds.containsKey(category)) {
            // Sensible fallback defaults
            return switch (category) {
                case HEALTHCARE -> new BigDecimal("5000.00");
                case UNEMPLOYMENT -> new BigDecimal("2500.00");
                case DISABILITY -> new BigDecimal("7500.00");
                case HOUSING -> new BigDecimal("4000.00");
                case CHILDCARE -> new BigDecimal("3000.00");
            };
        }
        return thresholds.get(category);
    }
}
