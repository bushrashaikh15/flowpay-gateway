package com.flowpay.flowpay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.entity.WebhookEvent;
import com.flowpay.flowpay.enums.PaymentEventType;
import com.flowpay.flowpay.repository.WebhookEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;

    private final String webhookSecret;

    public WebhookEventService(
            WebhookEventRepository webhookEventRepository,
            ObjectMapper objectMapper,
            @Value("${flowpay.webhook.secret}") String webhookSecret) {

        this.webhookEventRepository = webhookEventRepository;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    // ============================================================
    // CREATE WEBHOOK EVENT
    // ============================================================

    public WebhookEvent createWebhookEvent(
            PaymentIntent paymentIntent,
            PaymentEventType eventType) {

        try {

            // ====================================================
            // CREATE WEBHOOK PAYLOAD
            // ====================================================

            String payload =
                    objectMapper.writeValueAsString(
                            new WebhookPayload(
                                    paymentIntent.getId(),
                                    eventType,
                                    paymentIntent.getAmount(),
                                    paymentIntent.getCurrency(),
                                    paymentIntent.getStatus().name()
                            )
                    );

            // ====================================================
            // GENERATE HMAC-SHA256 SIGNATURE
            // ====================================================

            String signature =
                    generateSignature(payload);

            // ====================================================
            // CREATE WEBHOOK EVENT ENTITY
            // ====================================================

            WebhookEvent webhookEvent =
                    new WebhookEvent();

            webhookEvent.setPaymentIntent(
                    paymentIntent
            );

            webhookEvent.setEventType(
                    eventType
            );

            webhookEvent.setPayload(
                    payload
            );

            webhookEvent.setSignature(
                    signature
            );

            webhookEvent.setStatus(
                    "PENDING"
            );

            webhookEvent.setCreatedAt(
                    LocalDateTime.now()
            );

            // ====================================================
            // SAVE WEBHOOK EVENT
            // ====================================================

            return webhookEventRepository.save(
                    webhookEvent
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to create webhook payload",
                    e
            );
        }
    }

    // ============================================================
    // GENERATE HMAC-SHA256 SIGNATURE
    // ============================================================

    private String generateSignature(String payload) {

        try {

            Mac mac =
                    Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            webhookSecret.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );

            mac.init(secretKey);

            byte[] hash =
                    mac.doFinal(
                            payload.getBytes(StandardCharsets.UTF_8)
                    );

            return "sha256=" +
                    HexFormat.of().formatHex(hash);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate webhook signature",
                    e
            );
        }
    }

    // ============================================================
    // VERIFY WEBHOOK SIGNATURE
    // ============================================================

    public boolean verifySignature(
            String payload,
            String receivedSignature) {

        if (payload == null ||
                receivedSignature == null) {

            return false;
        }

        String expectedSignature =
                generateSignature(payload);

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ============================================================
    // WEBHOOK PAYLOAD
    // ============================================================

    private record WebhookPayload(
            Long paymentIntentId,
            PaymentEventType eventType,
            Object amount,
            String currency,
            String status
    ) {
    }
}