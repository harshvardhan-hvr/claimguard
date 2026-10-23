package com.claimguard.repository;

import com.claimguard.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByClaimIdOrderByEvaluatedAtDesc(Long claimId);
    Optional<RiskAssessment> findFirstByClaimIdOrderByEvaluatedAtDesc(Long claimId);
}
