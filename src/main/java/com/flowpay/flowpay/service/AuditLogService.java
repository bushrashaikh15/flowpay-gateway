package com.flowpay.flowpay.service;

import com.flowpay.flowpay.entity.AuditLog;
import com.flowpay.flowpay.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    // ============================================================
    // CREATE AUDIT LOG
    // ============================================================

    public void createAuditLog(
            Long paymentIntentId,
            String action,
            String description) {

        AuditLog auditLog = new AuditLog();

        auditLog.setPaymentIntentId(paymentIntentId);
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    // ============================================================
    // GET ALL AUDIT LOGS
    // ============================================================

    public List<AuditLog> getAllAuditLogs() {

        return auditLogRepository.findAll();
    }

    // ============================================================
    // GET AUDIT LOGS BY PAYMENT INTENT
    // ============================================================

    public List<AuditLog> getAuditLogsByPaymentIntentId(
            Long paymentIntentId) {

        return auditLogRepository
                .findByPaymentIntentIdOrderByCreatedAtAsc(
                        paymentIntentId
                );
    }
}