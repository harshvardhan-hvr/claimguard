package com.claimguard.entity;

import com.claimguard.enums.BenefitCategory;
import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.RiskLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_reference", nullable = false, unique = true, length = 50)
    private String claimReference;

    @Column(name = "claimant_id", nullable = false, length = 50)
    private String claimantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BenefitCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "submission_date", nullable = false)
    private LocalDate submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @Column(name = "risk_score", nullable = false)
    private int riskScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("evaluatedAt DESC")
    private List<RiskAssessment> riskAssessments = new ArrayList<>();

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<ClaimStatusHistory> statusHistories = new ArrayList<>();

    public Claim() {
    }

    public Claim(String claimReference, String claimantId, BenefitCategory category,
                 BigDecimal amount, LocalDate submissionDate, String description) {
        this.claimReference = claimReference;
        this.claimantId = claimantId;
        this.category = category;
        this.amount = amount;
        this.submissionDate = submissionDate;
        this.description = description;
        this.status = ClaimStatus.SUBMITTED;
        this.riskScore = 0;
        this.riskLevel = RiskLevel.LOW;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClaimReference() {
        return claimReference;
    }

    public void setClaimReference(String claimReference) {
        this.claimReference = claimReference;
    }

    public String getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(String claimantId) {
        this.claimantId = claimantId;
    }

    public BenefitCategory getCategory() {
        return category;
    }

    public void setCategory(BenefitCategory category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<RiskAssessment> getRiskAssessments() {
        return riskAssessments;
    }

    public void setRiskAssessments(List<RiskAssessment> riskAssessments) {
        this.riskAssessments = riskAssessments;
    }

    public List<ClaimStatusHistory> getStatusHistories() {
        return statusHistories;
    }

    public void setStatusHistories(List<ClaimStatusHistory> statusHistories) {
        this.statusHistories = statusHistories;
    }

    public void addRiskAssessment(RiskAssessment assessment) {
        this.riskAssessments.add(assessment);
        assessment.setClaim(this);
    }

    public void addStatusHistory(ClaimStatusHistory history) {
        this.statusHistories.add(history);
        history.setClaim(this);
    }
}
