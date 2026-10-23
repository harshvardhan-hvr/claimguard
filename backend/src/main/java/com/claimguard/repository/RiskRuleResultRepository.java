package com.claimguard.repository;

import com.claimguard.entity.RiskRuleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskRuleResultRepository extends JpaRepository<RiskRuleResult, Long> {
    List<RiskRuleResult> findByRiskAssessmentId(Long riskAssessmentId);
}
