package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.PaymentIntent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PaymentIntentRepository
        extends JpaRepository<PaymentIntent, Long>,
        JpaSpecificationExecutor<PaymentIntent> {

    List<PaymentIntent> findByMerchant_Id(Long merchantId);
}