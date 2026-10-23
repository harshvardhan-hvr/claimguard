package com.claimguard.service;

import com.claimguard.dto.request.CreateClaimRequest;
import com.claimguard.dto.request.UpdateClaimRequest;
import com.claimguard.dto.response.*;
import com.claimguard.entity.Claim;
import com.claimguard.entity.ClaimStatusHistory;
import com.claimguard.entity.RiskAssessment;
import com.claimguard.entity.RiskRuleResult;
import com.claimguard.enums.BenefitCategory;
import com.claimguard.enums.ClaimStatus;
import com.claimguard.enums.RiskLevel;
import com.claimguard.exception.DuplicateResourceException;
import com.claimguard.exception.InvalidTransitionException;
import com.claimguard.exception.ResourceNotFoundException;
import com.claimguard.repository.ClaimRepository;
import com.claimguard.repository.ClaimStatusHistoryRepository;
import com.claimguard.repository.RiskAssessmentRepository;
import com.claimguard.security.SecurityUtils;
import com.claimguard.service.risk.RiskEngine;
import com.claimguard.service.risk.RiskEvaluationContext;
import com.claimguard.service.risk.RiskEvaluationResult;
import com.claimguard.service.risk.RuleEvaluationResult;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ClaimStatusHistoryRepository historyRepository;
    private final RiskEngine riskEngine;
    private final AuditService auditService;

    public ClaimService(ClaimRepository claimRepository,
                        RiskAssessmentRepository riskAssessmentRepository,
                        ClaimStatusHistoryRepository historyRepository,
                        RiskEngine riskEngine,
                        AuditService auditService) {
        this.claimRepository = claimRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.historyRepository = historyRepository;
        this.riskEngine = riskEngine;
        this.auditService = auditService;
    }

    @Transactional
    public ClaimDetailResponse createClaim(CreateClaimRequest request) {
        String reference = request.getClaimReference();
        if (reference == null || reference.trim().isEmpty()) {
            reference = generateClaimReference();
        } else {
            reference = reference.trim().toUpperCase();
            if (claimRepository.existsByClaimReference(reference)) {
                throw new DuplicateResourceException("A claim with reference '" + reference + "' already exists.");
            }
        }

        Claim claim = new Claim(
                reference,
                request.getClaimantId().trim().toUpperCase(),
                request.getCategory(),
                request.getAmount(),
                request.getSubmissionDate() != null ? request.getSubmissionDate() : LocalDate.now(),
                request.getDescription()
        );

        // Fetch historical claims by claimant for risk evaluation context
        List<Claim> claimantHistory = claimRepository.findByClaimantId(claim.getClaimantId());
        RiskEvaluationContext context = new RiskEvaluationContext(claimantHistory);

        // Evaluate risk
        RiskEvaluationResult evalResult = riskEngine.evaluate(claim, context);
        claim.setRiskScore(evalResult.getTotalScore());
        claim.setRiskLevel(evalResult.getRiskLevel());

        // Flag automatically for review if risk is HIGH, otherwise set SUBMITTED
        if (evalResult.getRiskLevel() == RiskLevel.HIGH) {
            claim.setStatus(ClaimStatus.FLAGGED);
        } else {
            claim.setStatus(ClaimStatus.SUBMITTED);
        }

        Claim savedClaim = claimRepository.save(claim);

        // Save Risk Assessment
        RiskAssessment assessment = new RiskAssessment(
                savedClaim,
                evalResult.getTotalScore(),
                evalResult.getRiskLevel(),
                evalResult.getSummary()
        );

        for (RuleEvaluationResult ruleRes : evalResult.getRuleResults()) {
            RiskRuleResult ruleResultEntity = new RiskRuleResult(
                    ruleRes.getRuleName(),
                    ruleRes.getRuleCode(),
                    ruleRes.getScoreContribution(),
                    ruleRes.isTriggered(),
                    ruleRes.getExplanation()
            );
            assessment.addRuleResult(ruleResultEntity);
        }

        savedClaim.addRiskAssessment(assessment);
        riskAssessmentRepository.save(assessment);

        // Record Initial Status History
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String initialReason = evalResult.getRiskLevel() == RiskLevel.HIGH
                ? "Claim automatically flagged for review due to high risk assessment score (" + evalResult.getTotalScore() + "/100)."
                : "Initial claim submission.";

        ClaimStatusHistory initialHistory = new ClaimStatusHistory(
                savedClaim,
                ClaimStatus.SUBMITTED,
                savedClaim.getStatus(),
                currentUserId,
                currentUsername,
                initialReason
        );
        savedClaim.addStatusHistory(initialHistory);
        historyRepository.save(initialHistory);

        auditService.logAction(
                "Claim",
                savedClaim.getId(),
                "CREATE_CLAIM",
                String.format("Created claim %s with risk score %d (%s)", savedClaim.getClaimReference(), savedClaim.getRiskScore(), savedClaim.getRiskLevel())
        );

        return getClaimDetails(savedClaim.getId());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ClaimResponse> getClaims(
            String search,
            ClaimStatus status,
            BenefitCategory category,
            RiskLevel riskLevel,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Claim> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate refPredicate = cb.like(cb.lower(root.get("claimReference")), searchPattern);
                Predicate claimantPredicate = cb.like(cb.lower(root.get("claimantId")), searchPattern);
                predicates.add(cb.or(refPredicate, claimantPredicate));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (riskLevel != null) {
                predicates.add(cb.equal(root.get("riskLevel"), riskLevel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Claim> claimPage = claimRepository.findAll(spec, pageable);

        List<ClaimResponse> content = claimPage.getContent().stream()
                .map(this::mapToClaimResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                claimPage.getNumber(),
                claimPage.getSize(),
                claimPage.getTotalElements(),
                claimPage.getTotalPages(),
                claimPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ClaimDetailResponse getClaimDetails(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));

        ClaimDetailResponse response = new ClaimDetailResponse();
        copyPropertiesToResponse(claim, response);

        // Fetch latest risk assessment
        riskAssessmentRepository.findFirstByClaimIdOrderByEvaluatedAtDesc(id)
                .ifPresent(assessment -> response.setLatestRiskAssessment(mapToRiskAssessmentResponse(assessment)));

        // Fetch status history
        List<StatusHistoryResponse> historyResponses = historyRepository.findByClaimIdOrderByCreatedAtDesc(id).stream()
                .map(this::mapToStatusHistoryResponse)
                .collect(Collectors.toList());
        response.setStatusHistory(historyResponses);

        return response;
    }

    @Transactional
    public ClaimDetailResponse updateClaim(Long id, UpdateClaimRequest request) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));

        if (claim.getStatus() == ClaimStatus.CLEARED || claim.getStatus() == ClaimStatus.CONFIRMED_FRAUD) {
            throw new InvalidTransitionException("Cannot update claim information when claim is in finalized status '" + claim.getStatus() + "'.");
        }

        boolean shouldReevaluate = false;

        if (request.getCategory() != null && request.getCategory() != claim.getCategory()) {
            claim.setCategory(request.getCategory());
            shouldReevaluate = true;
        }

        if (request.getAmount() != null && request.getAmount().compareTo(claim.getAmount()) != 0) {
            claim.setAmount(request.getAmount());
            shouldReevaluate = true;
        }

        if (request.getDescription() != null) {
            claim.setDescription(request.getDescription());
        }

        if (shouldReevaluate) {
            evaluateAndPersistRisk(claim, "Re-evaluated following claim modification.");
        }

        claimRepository.save(claim);

        auditService.logAction(
                "Claim",
                claim.getId(),
                "UPDATE_CLAIM",
                "Updated claim details. Re-evaluated: " + shouldReevaluate
        );

        return getClaimDetails(id);
    }

    @Transactional
    public RiskAssessmentResponse analyzeClaim(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));

        RiskAssessment assessment = evaluateAndPersistRisk(claim, "Manual re-analysis requested by reviewer.");
        claimRepository.save(claim);

        auditService.logAction(
                "Claim",
                claim.getId(),
                "ANALYZE_RISK",
                String.format("Re-analyzed claim risk. New score: %d (%s)", assessment.getTotalScore(), assessment.getRiskLevel())
        );

        return mapToRiskAssessmentResponse(assessment);
    }

    @Transactional(readOnly = true)
    public RiskAssessmentResponse getLatestRiskAssessment(Long claimId) {
        if (!claimRepository.existsById(claimId)) {
            throw new ResourceNotFoundException("Claim not found with ID: " + claimId);
        }

        RiskAssessment assessment = riskAssessmentRepository.findFirstByClaimIdOrderByEvaluatedAtDesc(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("No risk assessment found for claim ID: " + claimId));

        return mapToRiskAssessmentResponse(assessment);
    }

    private RiskAssessment evaluateAndPersistRisk(Claim claim, String evaluatorNote) {
        List<Claim> claimantHistory = claimRepository.findByClaimantId(claim.getClaimantId());
        RiskEvaluationContext context = new RiskEvaluationContext(claimantHistory);

        RiskEvaluationResult evalResult = riskEngine.evaluate(claim, context);
        claim.setRiskScore(evalResult.getTotalScore());
        claim.setRiskLevel(evalResult.getRiskLevel());

        RiskAssessment assessment = new RiskAssessment(
                claim,
                evalResult.getTotalScore(),
                evalResult.getRiskLevel(),
                evaluatorNote != null ? evaluatorNote : evalResult.getSummary()
        );

        for (RuleEvaluationResult ruleRes : evalResult.getRuleResults()) {
            RiskRuleResult ruleResultEntity = new RiskRuleResult(
                    ruleRes.getRuleName(),
                    ruleRes.getRuleCode(),
                    ruleRes.getScoreContribution(),
                    ruleRes.isTriggered(),
                    ruleRes.getExplanation()
            );
            assessment.addRuleResult(ruleResultEntity);
        }

        claim.addRiskAssessment(assessment);
        return riskAssessmentRepository.save(assessment);
    }

    private String generateClaimReference() {
        int year = LocalDate.now().getYear();
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        String reference = "CLM-" + year + "-" + randomSuffix;
        while (claimRepository.existsByClaimReference(reference)) {
            randomSuffix = String.format("%04d", new Random().nextInt(10000));
            reference = "CLM-" + year + "-" + randomSuffix;
        }
        return reference;
    }

    public ClaimResponse mapToClaimResponse(Claim claim) {
        ClaimResponse response = new ClaimResponse();
        copyPropertiesToResponse(claim, response);
        return response;
    }

    private void copyPropertiesToResponse(Claim claim, ClaimResponse response) {
        response.setId(claim.getId());
        response.setClaimReference(claim.getClaimReference());
        response.setClaimantId(claim.getClaimantId());
        response.setCategory(claim.getCategory());
        response.setAmount(claim.getAmount());
        response.setSubmissionDate(claim.getSubmissionDate());
        response.setStatus(claim.getStatus());
        response.setRiskScore(claim.getRiskScore());
        response.setRiskLevel(claim.getRiskLevel());
        response.setDescription(claim.getDescription());
        response.setCreatedAt(claim.getCreatedAt());
        response.setUpdatedAt(claim.getUpdatedAt());
    }

    public RiskAssessmentResponse mapToRiskAssessmentResponse(RiskAssessment assessment) {
        RiskAssessmentResponse response = new RiskAssessmentResponse();
        response.setId(assessment.getId());
        response.setClaimId(assessment.getClaim().getId());
        response.setTotalScore(assessment.getTotalScore());
        response.setRiskLevel(assessment.getRiskLevel());
        response.setEvaluatedAt(assessment.getEvaluatedAt());
        response.setEvaluatorNote(assessment.getEvaluatorNote());

        List<RiskRuleResultResponse> ruleResponses = assessment.getRuleResults().stream()
                .map(r -> new RiskRuleResultResponse(
                        r.getId(),
                        r.getRuleName(),
                        r.getRuleCode(),
                        r.getScoreContribution(),
                        r.isTriggered(),
                        r.getExplanation()
                ))
                .collect(Collectors.toList());
        response.setRuleResults(ruleResponses);

        return response;
    }

    private StatusHistoryResponse mapToStatusHistoryResponse(ClaimStatusHistory history) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getClaim().getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedByUserId(),
                history.getChangedByUsername(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
