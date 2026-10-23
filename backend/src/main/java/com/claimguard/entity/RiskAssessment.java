package com.claimguard.entity;

import com.claimguard.enums.RiskLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private LocalDateTime evaluatedAt;

    @Column(name = "evaluator_note", length = 255)
    private String evaluatorNote;

    @OneToMany(mappedBy = "riskAssessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RiskRuleResult> ruleResults = new ArrayList<>();

    public RiskAssessment() {
    }

    public RiskAssessment(Claim claim, int totalScore, RiskLevel riskLevel, String evaluatorNote) {
        this.claim = claim;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.evaluatorNote = evaluatorNote;
        this.evaluatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (evaluatedAt == null) {
            evaluatedAt = LocalDateTime.now();
        }
    }

    public void addRuleResult(RiskRuleResult result) {
        this.ruleResults.add(result);
        result.setRiskAssessment(this);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public String getEvaluatorNote() {
        return evaluatorNote;
    }

    public void setEvaluatorNote(String evaluatorNote) {
        this.evaluatorNote = evaluatorNote;
    }

    public List<RiskRuleResult> getRuleResults() {
        return ruleResults;
    }

    public void setRuleResults(List<RiskRuleResult> ruleResults) {
        this.ruleResults = ruleResults;
    }
}
