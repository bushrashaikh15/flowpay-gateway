package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByPaymentIntentIdOrderByCreatedAtAsc(
            Long paymentIntentId
    );

    @Query("""
            SELECT a
            FROM AuditLog a
            JOIN PaymentIntent p
                ON p.id = a.paymentIntentId
            WHERE p.merchant.id = :merchantId
            ORDER BY a.createdAt ASC
            """)
    List<AuditLog> findByMerchantId(
            @Param("merchantId") Long merchantId
    );
}