package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.PaymentIntentRequest;
import com.flowpay.flowpay.dto.PaymentIntentResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.enums.PaymentStatus;
import com.flowpay.flowpay.service.PaymentIntentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-intents")
public class PaymentIntentController {

    private final PaymentIntentService paymentIntentService;

    public PaymentIntentController(
            PaymentIntentService paymentIntentService) {

        this.paymentIntentService = paymentIntentService;
    }

    // ============================================================
    // CREATE PAYMENT INTENT
    // ============================================================

    @PostMapping
    public PaymentIntentResponse createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return paymentIntentService.createPaymentIntent(
                request,
                idempotencyKey,
                merchant
        );
    }

    // ============================================================
    // GET PAYMENT INTENTS
    // WITH FILTERING + PAGINATION
    // ============================================================

    @GetMapping
    public Page<PaymentIntentResponse> getAllPaymentIntents(
            Pageable pageable,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return paymentIntentService.getAllPaymentIntents(
                pageable,
                status,
                currency,
                minAmount,
                maxAmount,
                merchant
        );
    }

    // ============================================================
    // GET PAYMENT INTENT BY ID
    // ============================================================

    @GetMapping("/{id}")
    public PaymentIntentResponse getPaymentIntentById(
            @PathVariable Long id,
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return paymentIntentService.getPaymentIntentById(
                id,
                merchant
        );
    }

    // ============================================================
    // AUTHORIZE
    // ============================================================

    @PutMapping("/{id}/authorize")
    public PaymentIntentResponse authorizePaymentIntent(
            @PathVariable Long id,
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return paymentIntentService.authorizePaymentIntent(
                id,
                merchant
        );
    }

    // ============================================================
    // CAPTURE
    // ============================================================

    @PutMapping("/{id}/capture")
    public PaymentIntentResponse capturePaymentIntent(
            @PathVariable Long id,
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return paymentIntentService.capturePaymentIntent(
                id,
                merchant
        );
    }

    // ============================================================
    // REFUND
    // ============================================================

    @PutMapping("/{id}/refund")
    public PaymentIntentResponse refundPaymentIntent(
            @PathVariable Long id,
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return paymentIntentService.refundPaymentIntent(
                id,
                merchant
        );
    }
}