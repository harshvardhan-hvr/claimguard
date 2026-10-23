package com.claimguard.repository;

import com.claimguard.entity.Claim;
import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long>, JpaSpecificationExecutor<Claim> {

    Optional<Claim> findByClaimReference(String claimReference);

    boolean existsByClaimReference(String claimReference);

    List<Claim> findByClaimantId(String claimantId);

    List<Claim> findByClaimantIdAndSubmissionDateGreaterThanEqual(String claimantId, LocalDate date);

    long countByStatus(ClaimStatus status);

    long countByRiskLevel(RiskLevel riskLevel);

    @Query("SELECT c.status, COUNT(c) FROM Claim c GROUP BY c.status")
    List<Object[]> countClaimsGroupedByStatus();

    @Query("SELECT c.riskLevel, COUNT(c) FROM Claim c GROUP BY c.riskLevel")
    List<Object[]> countClaimsGroupedByRiskLevel();

    @Query("SELECT c FROM Claim c WHERE c.submissionDate BETWEEN :startDate AND :endDate ORDER BY c.submissionDate DESC")
    List<Claim> findClaimsSubmittedWithinRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM Claim c WHERE c.claimantId IN (" +
           "  SELECT c2.claimantId FROM Claim c2 GROUP BY c2.claimantId HAVING COUNT(c2) > 1" +
           ") ORDER BY c.claimantId, c.submissionDate DESC")
    List<Claim> findRepeatedClaimantClaims();

    @Query("SELECT c FROM Claim c WHERE c.status IN ('SUBMITTED', 'UNDER_REVIEW', 'FLAGGED') ORDER BY c.riskScore DESC, c.createdAt ASC")
    List<Claim> findClaimsAwaitingInvestigation();

    @Query("SELECT c FROM Claim c ORDER BY c.createdAt DESC")
    List<Claim> findRecentClaims(Pageable pageable);
}
