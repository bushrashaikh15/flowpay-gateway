package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.AnalyticsChatRequest;
import com.flowpay.flowpay.dto.AnalyticsChatResponse;
import com.flowpay.flowpay.dto.MonthlyPaymentReportResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.service.MerchantAnalyticsService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {


    private final MerchantAnalyticsService
            merchantAnalyticsService;


    public AnalyticsController(
            MerchantAnalyticsService
                    merchantAnalyticsService) {

        this.merchantAnalyticsService =
                merchantAnalyticsService;
    }


    /*
     * =========================================================
     * ASK AI ANALYTICS QUESTION
     * =========================================================
     */

    @PostMapping("/ask")
    public AnalyticsChatResponse askAnalyticsQuestion(

            @RequestBody
            AnalyticsChatRequest request,

            Authentication authentication) {


        Merchant merchant =
                (Merchant)
                        authentication
                                .getPrincipal();


        return merchantAnalyticsService
                .askAnalyticsQuestion(
                        request,
                        merchant
                );
    }


    /*
     * =========================================================
     * MONTHLY AI PAYMENT REPORT
     * =========================================================
     */

    @GetMapping("/monthly-report")
    public MonthlyPaymentReportResponse
    getMonthlyPaymentReport(

            @RequestParam int year,

            @RequestParam int month,

            Authentication authentication) {


        Merchant merchant =
                (Merchant)
                        authentication
                                .getPrincipal();


        return merchantAnalyticsService
                .generateMonthlyReport(
                        year,
                        month,
                        merchant
                );
    }
}