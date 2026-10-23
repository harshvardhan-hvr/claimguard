package com.claimguard.service;

import com.claimguard.entity.AuditLog;
import com.claimguard.repository.AuditLogRepository;
import com.claimguard.security.SecurityUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityName, Long entityId, String action, String details) {
        String currentUser = SecurityUtils.getCurrentUsername();
        AuditLog log = new AuditLog(entityName, entityId, action, currentUser, details);
        auditLogRepository.save(log);
    }
}
