package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.entity.AuditLog;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.service.AuditLogService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(
            AuditLogService auditLogService) {

        this.auditLogService = auditLogService;
    }

    // ============================================================
    // GET ALL AUDIT LOGS
    // ============================================================

    @GetMapping
    public List<AuditLog> getAllAuditLogs(
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return auditLogService.getAllAuditLogs(
                merchant
        );
    }

    // ============================================================
    // GET AUDIT LOGS BY PAYMENT INTENT
    // ============================================================

    @GetMapping("/payment/{paymentIntentId}")
    public List<AuditLog> getAuditLogsByPaymentIntentId(
            @PathVariable Long paymentIntentId,
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return auditLogService
                .getAuditLogsByPaymentIntentId(
                        paymentIntentId,
                        merchant
                );
    }
}