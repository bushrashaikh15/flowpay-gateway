package com.flowpay.flowpay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowpay.flowpay.dto.PaymentIntentRequest;
import com.flowpay.flowpay.dto.PaymentIntentResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.enums.PaymentEventType;
import com.flowpay.flowpay.enums.PaymentStatus;
import com.flowpay.flowpay.repository.MerchantRepository;
import com.flowpay.flowpay.repository.PaymentIntentRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final MerchantRepository merchantRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final WebhookEventService webhookEventService;

    public PaymentIntentService(
            PaymentIntentRepository paymentIntentRepository,
            MerchantRepository merchantRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            WebhookEventService webhookEventService) {

        this.paymentIntentRepository = paymentIntentRepository;
        this.merchantRepository = merchantRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.webhookEventService = webhookEventService;
    }

    // ============================================================
    // CREATE PAYMENT INTENT WITH IDEMPOTENCY
    // ============================================================

    public PaymentIntentResponse createPaymentIntent(
            PaymentIntentRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {

            throw new RuntimeException(
                    "Idempotency-Key header is required");
        }

        String redisKey =
                "idempotency:"
                        + request.getMerchantId()
                        + ":"
                        + idempotencyKey;

        Boolean keyCreated = redisTemplate
                .opsForValue()
                .setIfAbsent(
                        redisKey,
                        "PROCESSING",
                        Duration.ofMinutes(5)
                );

        // Idempotency key already exists
        if (Boolean.FALSE.equals(keyCreated)) {

            String cachedResponse =
                    redisTemplate
                            .opsForValue()
                            .get(redisKey);

            if ("PROCESSING".equals(cachedResponse)) {

                throw new RuntimeException(
                        "Payment request is already being processed");
            }

            if (cachedResponse != null) {

                try {

                    return objectMapper.readValue(
                            cachedResponse,
                            PaymentIntentResponse.class
                    );

                } catch (JsonProcessingException e) {

                    throw new RuntimeException(
                            "Failed to read cached payment response",
                            e
                    );
                }
            }
        }

        try {

            // Find merchant
            Merchant merchant =
                    merchantRepository.findById(
                            request.getMerchantId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Merchant not found"
                            )
                    );

            // Create payment intent
            PaymentIntent paymentIntent =
                    new PaymentIntent();

            paymentIntent.setAmount(request.getAmount());
            paymentIntent.setCurrency(request.getCurrency());
            paymentIntent.setStatus(PaymentStatus.CREATED);
            paymentIntent.setCreatedAt(LocalDateTime.now());
            paymentIntent.setMerchant(merchant);

            // Save to PostgreSQL
            PaymentIntent savedPayment =
                    paymentIntentRepository.save(paymentIntent);

            // Create webhook event
            webhookEventService.createWebhookEvent(
                    savedPayment,
                    PaymentEventType.PAYMENT_CREATED
            );

            // Convert Entity → Response DTO
            PaymentIntentResponse response =
                    mapToResponse(savedPayment);

            // Convert response to JSON
            String responseJson =
                    objectMapper.writeValueAsString(response);

            // Store final response in Redis
            redisTemplate.opsForValue().set(
                    redisKey,
                    responseJson,
                    Duration.ofHours(24)
            );

            return response;

        } catch (Exception e) {

            // Allow safe retry if creation failed
            redisTemplate.delete(redisKey);

            throw new RuntimeException(
                    "Failed to create payment intent",
                    e
            );
        }
    }

    // ============================================================
    // GET ALL PAYMENT INTENTS
    // ============================================================

    public List<PaymentIntentResponse> getAllPaymentIntents() {

        return paymentIntentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ============================================================
    // GET PAYMENT INTENT BY ID
    // ============================================================

    public PaymentIntentResponse getPaymentIntentById(Long id) {

        PaymentIntent paymentIntent =
                paymentIntentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment Intent not found"
                                )
                        );

        return mapToResponse(paymentIntent);
    }

    // ============================================================
    // AUTHORIZE PAYMENT INTENT
    // CREATED → AUTHORIZED
    // ============================================================

    public PaymentIntentResponse authorizePaymentIntent(Long id) {

        PaymentIntent paymentIntent =
                paymentIntentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment Intent not found"
                                )
                        );

        // Validate state transition
        if (paymentIntent.getStatus() != PaymentStatus.CREATED) {

            throw new RuntimeException(
                    "Payment can only be authorized from CREATED status"
            );
        }

        // Change state
        paymentIntent.setStatus(
                PaymentStatus.AUTHORIZED
        );

        // Save
        PaymentIntent savedPayment =
                paymentIntentRepository.save(paymentIntent);

        // Create webhook
        webhookEventService.createWebhookEvent(
                savedPayment,
                PaymentEventType.PAYMENT_AUTHORIZED
        );

        return mapToResponse(savedPayment);
    }

    // ============================================================
    // CAPTURE PAYMENT INTENT
    // AUTHORIZED → CAPTURED
    // ============================================================

    public PaymentIntentResponse capturePaymentIntent(Long id) {

        PaymentIntent paymentIntent =
                paymentIntentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment Intent not found"
                                )
                        );

        // Validate state transition
        if (paymentIntent.getStatus() != PaymentStatus.AUTHORIZED) {

            throw new RuntimeException(
                    "Payment can only be captured from AUTHORIZED status"
            );
        }

        // Change state
        paymentIntent.setStatus(
                PaymentStatus.CAPTURED
        );

        // Save
        PaymentIntent savedPayment =
                paymentIntentRepository.save(paymentIntent);

        // Create webhook
        webhookEventService.createWebhookEvent(
                savedPayment,
                PaymentEventType.PAYMENT_CAPTURED
        );

        return mapToResponse(savedPayment);
    }

    // ============================================================
    // REFUND PAYMENT INTENT
    // CAPTURED → REFUNDED
    // ============================================================

    public PaymentIntentResponse refundPaymentIntent(Long id) {

        PaymentIntent paymentIntent =
                paymentIntentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment Intent not found"
                                )
                        );

        // Validate state transition
        if (paymentIntent.getStatus() != PaymentStatus.CAPTURED) {

            throw new RuntimeException(
                    "Payment can only be refunded from CAPTURED status"
            );
        }

        // Change state
        paymentIntent.setStatus(
                PaymentStatus.REFUNDED
        );

        // Save
        PaymentIntent savedPayment =
                paymentIntentRepository.save(paymentIntent);

        // Create webhook
        webhookEventService.createWebhookEvent(
                savedPayment,
                PaymentEventType.PAYMENT_REFUNDED
        );

        return mapToResponse(savedPayment);
    }

    // ============================================================
    // ENTITY → RESPONSE DTO
    // ============================================================

    private PaymentIntentResponse mapToResponse(
            PaymentIntent paymentIntent) {

        PaymentIntentResponse response =
                new PaymentIntentResponse();

        response.setId(paymentIntent.getId());
        response.setAmount(paymentIntent.getAmount());
        response.setCurrency(paymentIntent.getCurrency());
        response.setStatus(paymentIntent.getStatus());
        response.setCreatedAt(paymentIntent.getCreatedAt());

        response.setMerchantName(
                paymentIntent
                        .getMerchant()
                        .getMerchantName()
        );

        return response;
    }
}