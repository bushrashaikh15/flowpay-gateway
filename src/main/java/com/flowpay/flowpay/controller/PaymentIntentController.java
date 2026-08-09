package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.PaymentIntentRequest;
import com.flowpay.flowpay.dto.PaymentIntentResponse;
import com.flowpay.flowpay.service.PaymentIntentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-intents")
public class PaymentIntentController {

    private final PaymentIntentService paymentIntentService;

    public PaymentIntentController(PaymentIntentService paymentIntentService) {
        this.paymentIntentService = paymentIntentService;
    }

    @PostMapping
    public PaymentIntentResponse createPaymentIntent(
            @RequestBody PaymentIntentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return paymentIntentService.createPaymentIntent(
                request,
                idempotencyKey
        );
    }

    @GetMapping
    public List<PaymentIntentResponse> getAllPaymentIntents() {
        return paymentIntentService.getAllPaymentIntents();
    }

    @GetMapping("/{id}")
    public PaymentIntentResponse getPaymentIntentById(
            @PathVariable Long id) {

        return paymentIntentService.getPaymentIntentById(id);
    }

    @PutMapping("/{id}/authorize")
    public PaymentIntentResponse authorizePaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.authorizePaymentIntent(id);
    }

    @PutMapping("/{id}/capture")
    public PaymentIntentResponse capturePaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.capturePaymentIntent(id);
    }

    @PutMapping("/{id}/refund")
    public PaymentIntentResponse refundPaymentIntent(
            @PathVariable Long id) {

        return paymentIntentService.refundPaymentIntent(id);
    }
}