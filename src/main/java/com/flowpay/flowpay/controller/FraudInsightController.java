package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.FraudInsightResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.service.FraudRiskService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraud-insights")
public class FraudInsightController {

    private final FraudRiskService fraudRiskService;


    public FraudInsightController(
            FraudRiskService fraudRiskService) {

        this.fraudRiskService =
                fraudRiskService;
    }


    @GetMapping("/payment/{paymentIntentId}")
    public FraudInsightResponse analyzePayment(
            @PathVariable Long paymentIntentId,
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();


        return fraudRiskService.analyzePayment(
                paymentIntentId,
                merchant
        );
    }
}