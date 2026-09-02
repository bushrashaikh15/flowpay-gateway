package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.entity.AuditLog;
import com.flowpay.flowpay.service.AuditLogService;
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
    public List<AuditLog> getAllAuditLogs() {

        return auditLogService.getAllAuditLogs();
    }

    // ============================================================
    // GET AUDIT LOGS BY PAYMENT INTENT
    // ============================================================

    @GetMapping("/payment/{paymentIntentId}")
    public List<AuditLog> getAuditLogsByPaymentIntentId(
            @PathVariable Long paymentIntentId) {

        return auditLogService
                .getAuditLogsByPaymentIntentId(
                        paymentIntentId
                );
    }
}