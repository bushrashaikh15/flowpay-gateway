package com.flowpay.flowpay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.entity.WebhookEvent;
import com.flowpay.flowpay.enums.PaymentEventType;
import com.flowpay.flowpay.repository.WebhookEventRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;

    private static final String WEBHOOK_SECRET =
            "flowpay-webhook-secret";

    public WebhookEventService(
            WebhookEventRepository webhookEventRepository,
            ObjectMapper objectMapper) {

        this.webhookEventRepository = webhookEventRepository;
        this.objectMapper = objectMapper;
    }

    public WebhookEvent createWebhookEvent(
            PaymentIntent paymentIntent,
            PaymentEventType eventType) {

        try {

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

            String signature =
                    generateSignature(payload);

            WebhookEvent webhookEvent =
                    new WebhookEvent();

            webhookEvent.setPaymentIntent(paymentIntent);
            webhookEvent.setEventType(eventType);
            webhookEvent.setPayload(payload);
            webhookEvent.setSignature(signature);
            webhookEvent.setStatus("PENDING");
            webhookEvent.setCreatedAt(LocalDateTime.now());

            return webhookEventRepository.save(webhookEvent);

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to create webhook payload",
                    e
            );
        }
    }

    private String generateSignature(String payload) {

        try {

            Mac mac =
                    Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );

            mac.init(secretKey);

            byte[] hash =
                    mac.doFinal(
                            payload.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate webhook signature",
                    e
            );
        }
    }

    private record WebhookPayload(
            Long paymentIntentId,
            PaymentEventType eventType,
            Object amount,
            String currency,
            String status
    ) {
    }
}