package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.PaymentIntentRequest;
import com.flowpay.flowpay.dto.PaymentIntentResponse;
import com.flowpay.flowpay.service.PaymentIntentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    // GET ALL PAYMENT INTENTS
    // ============================================================

    @GetMapping
    public List<PaymentIntentResponse> getAllPaymentIntents() {

        return paymentIntentService.getAllPaymentIntents();
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
    // CREATED → AUTHORIZED
    // ============================================================

    @PutMapping("/{id}/authorize")
    public PaymentIntentResponse authorizePaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.authorizePaymentIntent(id);
    }

    // ============================================================
    // CAPTURE
    // AUTHORIZED → CAPTURED
    // ============================================================

    @PutMapping("/{id}/capture")
    public PaymentIntentResponse capturePaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.capturePaymentIntent(id);
    }

    // ============================================================
    // REFUND
    // CAPTURED → REFUNDED
    // ============================================================

    @PutMapping("/{id}/refund")
    public PaymentIntentResponse refundPaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.refundPaymentIntent(id);
    }
}