package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentIntentRepository
        extends JpaRepository<PaymentIntent, Long>,
        JpaSpecificationExecutor<PaymentIntent> {

}