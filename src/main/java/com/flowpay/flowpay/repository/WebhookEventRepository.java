package com.flowpay.flowpay.repository;

import com.flowpay.flowpay.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository
        extends JpaRepository<WebhookEvent, Long> {
}