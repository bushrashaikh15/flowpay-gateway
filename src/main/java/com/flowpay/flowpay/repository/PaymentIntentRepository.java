package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.PaymentIntent;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentIntentRepository
        extends JpaRepository<PaymentIntent, Long>,
        JpaSpecificationExecutor<PaymentIntent> {

    List<PaymentIntent> findByMerchant_Id(Long merchantId);

    // ============================================================
    // DASHBOARD SUMMARY
    // ============================================================

    @Query("""
            SELECT
                COUNT(paymentIntent),
                SUM(CASE
                    WHEN paymentIntent.status = com.flowpay.flowpay.enums.PaymentStatus.CREATED
                    THEN 1 ELSE 0
                END),
                SUM(CASE
                    WHEN paymentIntent.status = com.flowpay.flowpay.enums.PaymentStatus.AUTHORIZED
                    THEN 1 ELSE 0
                END),
                SUM(CASE
                    WHEN paymentIntent.status = com.flowpay.flowpay.enums.PaymentStatus.CAPTURED
                    THEN 1 ELSE 0
                END),
                SUM(CASE
                    WHEN paymentIntent.status = com.flowpay.flowpay.enums.PaymentStatus.REFUNDED
                    THEN 1 ELSE 0
                END),
                SUM(CASE
                    WHEN paymentIntent.status = com.flowpay.flowpay.enums.PaymentStatus.FAILED
                    THEN 1 ELSE 0
                END),
                COALESCE(
                    SUM(CASE
                        WHEN paymentIntent.status = com.flowpay.flowpay.enums.PaymentStatus.CAPTURED
                        THEN paymentIntent.amount
                        ELSE 0
                    END),
                    0
                ),
                COALESCE(
                    SUM(CASE
                        WHEN paymentIntent.status = com.flowpay.flowpay.enums.PaymentStatus.REFUNDED
                        THEN paymentIntent.amount
                        ELSE 0
                    END),
                    0
                )
            FROM PaymentIntent paymentIntent
            WHERE paymentIntent.merchant.id = :merchantId
            """)
    List<Object[]> getDashboardStatistics(
            @Param("merchantId") Long merchantId
    );

    // ============================================================
    // MOST USED CURRENCY
    // ============================================================

    @Query("""
            SELECT paymentIntent.currency
            FROM PaymentIntent paymentIntent
            WHERE paymentIntent.merchant.id = :merchantId
            GROUP BY paymentIntent.currency
            ORDER BY COUNT(paymentIntent) DESC
            """)
    List<String> findMostUsedCurrency(
            @Param("merchantId") Long merchantId,
            Pageable pageable
    );
}