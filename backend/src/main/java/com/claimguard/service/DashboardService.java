package com.claimguard.service;

import com.claimguard.dto.response.ClaimResponse;
import com.claimguard.dto.response.ClaimsByStatusResponse;
import com.claimguard.dto.response.DashboardSummaryResponse;
import com.claimguard.dto.response.RiskDistributionResponse;
import com.claimguard.entity.Claim;
import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.RiskLevel;
import com.claimguard.repository.ClaimRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ClaimRepository claimRepository;
    private final ClaimService claimService;

    public DashboardService(ClaimRepository claimRepository, ClaimService claimService) {
        this.claimRepository = claimRepository;
        this.claimService = claimService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        long totalClaims = claimRepository.count();

        long submitted = claimRepository.countByStatus(ClaimStatus.SUBMITTED);
        long underReview = claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW);
        long awaitingReview = submitted + underReview;

        long highRiskClaims = claimRepository.countByRiskLevel(RiskLevel.HIGH);
        long cleared = claimRepository.countByStatus(ClaimStatus.CLEARED);
        long fraud = claimRepository.countByStatus(ClaimStatus.CONFIRMED_FRAUD);
        long reviewedClaims = cleared + fraud;

        List<Claim> allClaims = claimRepository.findAll();
        BigDecimal totalClaimedAmount = BigDecimal.ZERO;
        BigDecimal confirmedFraudAmount = BigDecimal.ZERO;
        BigDecimal clearedAmount = BigDecimal.ZERO;

        for (Claim c : allClaims) {
            if (c.getAmount() != null) {
                totalClaimedAmount = totalClaimedAmount.add(c.getAmount());
                if (c.getStatus() == ClaimStatus.CONFIRMED_FRAUD) {
                    confirmedFraudAmount = confirmedFraudAmount.add(c.getAmount());
                } else if (c.getStatus() == ClaimStatus.CLEARED) {
                    clearedAmount = clearedAmount.add(c.getAmount());
                }
            }
        }

        return new DashboardSummaryResponse(
                totalClaims,
                awaitingReview,
                highRiskClaims,
                reviewedClaims,
                totalClaimedAmount,
                confirmedFraudAmount,
                clearedAmount
        );
    }

    @Transactional(readOnly = true)
    public ClaimsByStatusResponse getClaimsByStatus() {
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (ClaimStatus status : ClaimStatus.values()) {
            statusMap.put(status.name(), 0L);
        }

        List<Object[]> results = claimRepository.countClaimsGroupedByStatus();
        for (Object[] row : results) {
            ClaimStatus status = (ClaimStatus) row[0];
            Long count = (Long) row[1];
            statusMap.put(status.name(), count);
        }

        return new ClaimsByStatusResponse(statusMap);
    }

    @Transactional(readOnly = true)
    public RiskDistributionResponse getRiskDistribution() {
        Map<String, Long> riskMap = new LinkedHashMap<>();
        for (RiskLevel level : RiskLevel.values()) {
            riskMap.put(level.name(), 0L);
        }

        List<Object[]> results = claimRepository.countClaimsGroupedByRiskLevel();
        for (Object[] row : results) {
            RiskLevel level = (RiskLevel) row[0];
            Long count = (Long) row[1];
            riskMap.put(level.name(), count);
        }

        return new RiskDistributionResponse(riskMap);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getRecentClaims(int limit) {
        return claimRepository.findRecentClaims(PageRequest.of(0, limit)).stream()
                .map(claimService::mapToClaimResponse)
                .collect(Collectors.toList());
    }
}
