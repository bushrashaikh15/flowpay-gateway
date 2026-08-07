package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

}