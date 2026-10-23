package com.claimguard.dto.response;

import java.math.BigDecimal;

public class DashboardSummaryResponse {
    private long totalClaims;
    private long awaitingReview;
    private long highRiskClaims;
    private long reviewedClaims;
    private BigDecimal totalClaimedAmount;
    private BigDecimal confirmedFraudAmount;
    private BigDecimal clearedAmount;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(long totalClaims, long awaitingReview, long highRiskClaims,
                                    long reviewedClaims, BigDecimal totalClaimedAmount,
                                    BigDecimal confirmedFraudAmount, BigDecimal clearedAmount) {
        this.totalClaims = totalClaims;
        this.awaitingReview = awaitingReview;
        this.highRiskClaims = highRiskClaims;
        this.reviewedClaims = reviewedClaims;
        this.totalClaimedAmount = totalClaimedAmount;
        this.confirmedFraudAmount = confirmedFraudAmount;
        this.clearedAmount = clearedAmount;
    }

    public long getTotalClaims() {
        return totalClaims;
    }

    public void setTotalClaims(long totalClaims) {
        this.totalClaims = totalClaims;
    }

    public long getAwaitingReview() {
        return awaitingReview;
    }

    public void setAwaitingReview(long awaitingReview) {
        this.awaitingReview = awaitingReview;
    }

    public long getHighRiskClaims() {
        return highRiskClaims;
    }

    public void setHighRiskClaims(long highRiskClaims) {
        this.highRiskClaims = highRiskClaims;
    }

    public long getReviewedClaims() {
        return reviewedClaims;
    }

    public void setReviewedClaims(long reviewedClaims) {
        this.reviewedClaims = reviewedClaims;
    }

    public BigDecimal getTotalClaimedAmount() {
        return totalClaimedAmount;
    }

    public void setTotalClaimedAmount(BigDecimal totalClaimedAmount) {
        this.totalClaimedAmount = totalClaimedAmount;
    }

    public BigDecimal getConfirmedFraudAmount() {
        return confirmedFraudAmount;
    }

    public void setConfirmedFraudAmount(BigDecimal confirmedFraudAmount) {
        this.confirmedFraudAmount = confirmedFraudAmount;
    }

    public BigDecimal getClearedAmount() {
        return clearedAmount;
    }

    public void setClearedAmount(BigDecimal clearedAmount) {
        this.clearedAmount = clearedAmount;
    }
}
