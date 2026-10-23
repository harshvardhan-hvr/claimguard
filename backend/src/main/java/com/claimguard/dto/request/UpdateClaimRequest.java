package com.claimguard.dto.request;

import com.claimguard.enums.BenefitCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateClaimRequest {

    private BenefitCategory category;

    @DecimalMin(value = "0.01", message = "Claim amount must be strictly greater than 0")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public UpdateClaimRequest() {
    }

    public UpdateClaimRequest(BenefitCategory category, BigDecimal amount, String description) {
        this.category = category;
        this.amount = amount;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
