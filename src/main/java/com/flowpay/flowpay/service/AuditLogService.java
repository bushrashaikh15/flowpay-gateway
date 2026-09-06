package com.flowpay.flowpay.service;

import com.flowpay.flowpay.entity.AuditLog;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.exception.PaymentNotFoundException;
import com.flowpay.flowpay.exception.UnauthorizedResourceException;
import com.flowpay.flowpay.repository.AuditLogRepository;
import com.flowpay.flowpay.repository.PaymentIntentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final PaymentIntentRepository paymentIntentRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            PaymentIntentRepository paymentIntentRepository) {

        this.auditLogRepository = auditLogRepository;
        this.paymentIntentRepository =
                paymentIntentRepository;
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
    // GET ALL AUDIT LOGS FOR AUTHENTICATED MERCHANT
    // ============================================================

    public List<AuditLog> getAllAuditLogs(
            Merchant authenticatedMerchant) {

        return auditLogRepository.findByMerchantId(
                authenticatedMerchant.getId()
        );
    }

    // ============================================================
    // GET AUDIT LOGS BY PAYMENT INTENT
    // ============================================================

    public List<AuditLog> getAuditLogsByPaymentIntentId(
            Long paymentIntentId,
            Merchant authenticatedMerchant) {

        verifyPaymentIntentOwnership(
                paymentIntentId,
                authenticatedMerchant
        );

        return auditLogRepository
                .findByPaymentIntentIdOrderByCreatedAtAsc(
                        paymentIntentId
                );
    }

    // ============================================================
    // VERIFY PAYMENT INTENT OWNERSHIP
    // ============================================================

    private void verifyPaymentIntentOwnership(
            Long paymentIntentId,
            Merchant authenticatedMerchant) {

        PaymentIntent paymentIntent =
                paymentIntentRepository
                        .findById(paymentIntentId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment intent not found with id: "
                                                + paymentIntentId
                                )
                        );

        if (paymentIntent.getMerchant() == null
                || !paymentIntent
                .getMerchant()
                .getId()
                .equals(
                        authenticatedMerchant.getId()
                )) {

            throw new UnauthorizedResourceException(
                    "You are not authorized to access these audit logs"
            );
        }
    }
}