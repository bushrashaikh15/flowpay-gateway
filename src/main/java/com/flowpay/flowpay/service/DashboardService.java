package com.flowpay.flowpay.service;

import com.flowpay.flowpay.dto.DashboardSummaryResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.repository.PaymentIntentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private static final Logger logger =
            LoggerFactory.getLogger(DashboardService.class);

    private final PaymentIntentRepository paymentIntentRepository;

    public DashboardService(
            PaymentIntentRepository paymentIntentRepository) {

        this.paymentIntentRepository =
                paymentIntentRepository;
    }

    // ============================================================
    // GET DASHBOARD SUMMARY
    // ============================================================

    public DashboardSummaryResponse getDashboardSummary(
            Merchant authenticatedMerchant) {

        Long merchantId =
                authenticatedMerchant.getId();

        logger.info(
                "Generating dashboard summary for merchant: {}",
                merchantId
        );

        // ========================================================
        // GET AGGREGATED PAYMENT STATISTICS
        // ========================================================

        List<Object[]> statisticsResult =
                paymentIntentRepository.getDashboardStatistics(
                        merchantId
                );

        /*
         * The dashboard query always returns one row.
         *
         * That row contains:
         *
         * [0] total payments
         * [1] created payments
         * [2] authorized payments
         * [3] captured payments
         * [4] refunded payments
         * [5] failed payments
         * [6] captured amount
         * [7] refunded amount
         */

        Object[] statistics =
                statisticsResult.isEmpty()
                        ? new Object[8]
                        : statisticsResult.get(0);

        long totalPayments =
                toLong(statistics[0]);

        long createdPayments =
                toLong(statistics[1]);

        long authorizedPayments =
                toLong(statistics[2]);

        long capturedPayments =
                toLong(statistics[3]);

        long refundedPayments =
                toLong(statistics[4]);

        long failedPayments =
                toLong(statistics[5]);

        BigDecimal capturedAmount =
                toBigDecimal(statistics[6]);

        BigDecimal refundedAmount =
                toBigDecimal(statistics[7]);

        // ========================================================
        // MOST USED CURRENCY
        // ========================================================

        List<String> currencies =
                paymentIntentRepository.findMostUsedCurrency(
                        merchantId,
                        PageRequest.of(0, 1)
                );

        String mostUsedCurrency =
                currencies.isEmpty()
                        ? null
                        : currencies.get(0);

        // ========================================================
        // BUILD RESPONSE
        // ========================================================

        DashboardSummaryResponse response =
                new DashboardSummaryResponse(
                        totalPayments,
                        createdPayments,
                        authorizedPayments,
                        capturedPayments,
                        refundedPayments,
                        failedPayments,
                        capturedAmount,
                        refundedAmount,
                        mostUsedCurrency
                );

        logger.info(
                "Dashboard summary generated successfully for merchant: {}",
                merchantId
        );

        return response;
    }

    // ============================================================
    // SAFE NUMBER CONVERSION
    // ============================================================

    private long toLong(Object value) {

        if (value == null) {
            return 0L;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return 0L;
    }

    // ============================================================
    // SAFE BIGDECIMAL CONVERSION
    // ============================================================

    private BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            return BigDecimal.valueOf(
                    number.doubleValue()
            );
        }

        return BigDecimal.ZERO;
    }
}