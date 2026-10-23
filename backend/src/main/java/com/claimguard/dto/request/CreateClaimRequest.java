package com.claimguard.dto.request;

import com.claimguard.enums.BenefitCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateClaimRequest {

    private String claimReference;

    @NotBlank(message = "Claimant ID is required")
    @Size(max = 50, message = "Claimant ID cannot exceed 50 characters")
    private String claimantId;

    @NotNull(message = "Benefit category is required")
    private BenefitCategory category;

    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.01", message = "Claim amount must be strictly greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Submission date is required")
    @PastOrPresent(message = "Submission date cannot be in the future")
    private LocalDate submissionDate;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public CreateClaimRequest() {
    }

    public CreateClaimRequest(String claimReference, String claimantId, BenefitCategory category,
                              BigDecimal amount, LocalDate submissionDate, String description) {
        this.claimReference = claimReference;
        this.claimantId = claimantId;
        this.category = category;
        this.amount = amount;
        this.submissionDate = submissionDate;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
