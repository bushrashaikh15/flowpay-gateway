package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.PaymentIntentRequest;
import com.flowpay.flowpay.dto.PaymentIntentResponse;
import com.flowpay.flowpay.enums.PaymentStatus;
import com.flowpay.flowpay.service.PaymentIntentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return paymentIntentService.createPaymentIntent(
                request,
                idempotencyKey
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
            @RequestParam(required = false) Double maxAmount) {

        return paymentIntentService.getAllPaymentIntents(
                pageable,
                status,
                currency,
                minAmount,
                maxAmount
        );
    }

    // ============================================================
    // GET PAYMENT INTENT BY ID
    // ============================================================

    @GetMapping("/{id}")
    public PaymentIntentResponse getPaymentIntentById(
            @PathVariable Long id) {

        return paymentIntentService.getPaymentIntentById(id);
    }

    // ============================================================
    // AUTHORIZE
    // ============================================================

    @PutMapping("/{id}/authorize")
    public PaymentIntentResponse authorizePaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.authorizePaymentIntent(id);
    }

    // ============================================================
    // CAPTURE
    // ============================================================

    @PutMapping("/{id}/capture")
    public PaymentIntentResponse capturePaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.capturePaymentIntent(id);
    }

    // ============================================================
    // REFUND
    // ============================================================

    @PutMapping("/{id}/refund")
    public PaymentIntentResponse refundPaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.refundPaymentIntent(id);
    }
}