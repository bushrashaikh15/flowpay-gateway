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

    public PaymentIntentController(PaymentIntentService paymentIntentService) {
        this.paymentIntentService = paymentIntentService;
    }

    // Create Payment Intent
    @PostMapping
    public PaymentIntentResponse createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request) {

        return paymentIntentService.createPaymentIntent(request);
    }

    // Get All Payment Intents
    @GetMapping
    public List<PaymentIntentResponse> getAllPaymentIntents() {
        return paymentIntentService.getAllPaymentIntents();
    }

    // Get Payment Intent By ID
    @GetMapping("/{id}")
    public PaymentIntentResponse getPaymentIntentById(@PathVariable Long id) {
        return paymentIntentService.getPaymentIntentById(id);
    }
}