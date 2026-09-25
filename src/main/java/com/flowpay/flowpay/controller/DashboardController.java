package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.DashboardSummaryResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.service.DashboardService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService) {

        this.dashboardService =
                dashboardService;
    }

    // ============================================================
    // DASHBOARD SUMMARY
    // ============================================================

    @GetMapping("/summary")
    public DashboardSummaryResponse getDashboardSummary(
            Authentication authentication) {

        Merchant merchant =
                (Merchant) authentication.getPrincipal();

        return dashboardService.getDashboardSummary(
                merchant
        );
    }
}