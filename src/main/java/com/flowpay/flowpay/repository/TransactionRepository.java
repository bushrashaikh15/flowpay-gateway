package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByPaymentIntentId(Long paymentIntentId);
}