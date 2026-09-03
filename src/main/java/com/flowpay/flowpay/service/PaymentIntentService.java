package com.flowpay.flowpay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowpay.flowpay.dto.PaymentIntentRequest;
import com.flowpay.flowpay.dto.PaymentIntentResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.entity.Transaction;
import com.flowpay.flowpay.enums.PaymentEventType;
import com.flowpay.flowpay.enums.PaymentStatus;
import com.flowpay.flowpay.enums.TransactionType;
import com.flowpay.flowpay.exception.MerchantNotFoundException;
import com.flowpay.flowpay.exception.PaymentNotFoundException;
import com.flowpay.flowpay.repository.MerchantRepository;
import com.flowpay.flowpay.repository.PaymentIntentRepository;
import com.flowpay.flowpay.repository.TransactionRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final WebhookEventService webhookEventService;
    private final AuditLogService auditLogService;

    public PaymentIntentService(
            PaymentIntentRepository paymentIntentRepository,
            MerchantRepository merchantRepository,
            TransactionRepository transactionRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            WebhookEventService webhookEventService,
            AuditLogService auditLogService) {

        this.paymentIntentRepository = paymentIntentRepository;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.webhookEventService = webhookEventService;
        this.auditLogService = auditLogService;
    }

    // ============================================================
    // CREATE PAYMENT INTENT WITH IDEMPOTENCY
    // ============================================================

    public PaymentIntentResponse createPaymentIntent(
            PaymentIntentRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {

            throw new RuntimeException(
                    "Idempotency-Key header is required"
            );
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

        // ========================================================
        // IDEMPOTENCY KEY ALREADY EXISTS
        // ========================================================

        if (Boolean.FALSE.equals(keyCreated)) {

            String cachedResponse =
                    redisTemplate
                            .opsForValue()
                            .get(redisKey);

            if ("PROCESSING".equals(cachedResponse)) {

                throw new RuntimeException(
                        "Payment request is already being processed"
                );
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

            // ====================================================
            // FIND MERCHANT
            // ====================================================

            Merchant merchant =
                    merchantRepository.findById(
                            request.getMerchantId()
                    ).orElseThrow(() ->
                            new MerchantNotFoundException(
                                    "Merchant not found with id: "
                                            + request.getMerchantId()
                            )
                    );

            // ====================================================
            // CREATE PAYMENT INTENT
            // ====================================================

            PaymentIntent paymentIntent =
                    new PaymentIntent();

            paymentIntent.setAmount(
                    request.getAmount()
            );

            paymentIntent.setCurrency(
                    request.getCurrency()
            );

            paymentIntent.setStatus(
                    PaymentStatus.CREATED
            );

            paymentIntent.setCreatedAt(
                    LocalDateTime.now()
            );

            paymentIntent.setMerchant(
                    merchant
            );

            // ====================================================
            // SAVE PAYMENT INTENT
            // ====================================================

            PaymentIntent savedPayment =
                    paymentIntentRepository.save(
                            paymentIntent
                    );

            // ====================================================
            // CREATE WEBHOOK EVENT
            // ====================================================

            webhookEventService.createWebhookEvent(
                    savedPayment,
                    PaymentEventType.PAYMENT_CREATED
            );

            // ====================================================
            // CREATE AUDIT LOG
            // ====================================================

            auditLogService.createAuditLog(
                    savedPayment.getId(),
                    "PAYMENT_CREATED",
                    "Payment intent created"
            );

            // ====================================================
            // CONVERT ENTITY → RESPONSE DTO
            // ====================================================

            PaymentIntentResponse response =
                    mapToResponse(
                            savedPayment
                    );

            // ====================================================
            // CONVERT RESPONSE → JSON
            // ====================================================

            String responseJson =
                    objectMapper.writeValueAsString(
                            response
                    );

            // ====================================================
            // STORE FINAL RESPONSE IN REDIS
            // ====================================================

            redisTemplate.opsForValue().set(
                    redisKey,
                    responseJson,
                    Duration.ofHours(24)
            );

            return response;

        } catch (MerchantNotFoundException e) {

            // Allow safe retry if merchant was invalid
            redisTemplate.delete(redisKey);

            throw e;

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
                                new PaymentNotFoundException(
                                        "Payment intent not found with id: "
                                                + id
                                )
                        );

        return mapToResponse(
                paymentIntent
        );
    }

    // ============================================================
    // AUTHORIZE PAYMENT INTENT
    // CREATED → AUTHORIZED
    // ============================================================

    @Transactional
    public PaymentIntentResponse authorizePaymentIntent(Long id) {

        PaymentIntent paymentIntent =
                paymentIntentRepository.findById(id)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment intent not found with id: "
                                                + id
                                )
                        );

        // ========================================================
        // VALIDATE STATE TRANSITION
        // ========================================================

        if (paymentIntent.getStatus()
                != PaymentStatus.CREATED) {

            throw new RuntimeException(
                    "Payment can only be authorized from CREATED status"
            );
        }

        // ========================================================
        // CHANGE STATUS
        // ========================================================

        paymentIntent.setStatus(
                PaymentStatus.AUTHORIZED
        );

        // ========================================================
        // SAVE PAYMENT
        // ========================================================

        PaymentIntent savedPayment =
                paymentIntentRepository.save(
                        paymentIntent
                );

        // ========================================================
        // CREATE WEBHOOK EVENT
        // ========================================================

        webhookEventService.createWebhookEvent(
                savedPayment,
                PaymentEventType.PAYMENT_AUTHORIZED
        );

        // ========================================================
        // CREATE AUDIT LOG
        // ========================================================

        auditLogService.createAuditLog(
                savedPayment.getId(),
                "PAYMENT_AUTHORIZED",
                "Payment intent authorized"
        );

        return mapToResponse(
                savedPayment
        );
    }

    // ============================================================
    // CAPTURE PAYMENT INTENT
    // AUTHORIZED → CAPTURED
    // ============================================================

    @Transactional
    public PaymentIntentResponse capturePaymentIntent(Long id) {

        PaymentIntent paymentIntent =
                paymentIntentRepository.findById(id)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment intent not found with id: "
                                                + id
                                )
                        );

        // ========================================================
        // VALIDATE STATE TRANSITION
        // ========================================================

        if (paymentIntent.getStatus()
                != PaymentStatus.AUTHORIZED) {

            throw new RuntimeException(
                    "Payment can only be captured from AUTHORIZED status"
            );
        }

        // ========================================================
        // CHANGE STATUS
        // ========================================================

        paymentIntent.setStatus(
                PaymentStatus.CAPTURED
        );

        // ========================================================
        // SAVE PAYMENT
        // ========================================================

        PaymentIntent savedPayment =
                paymentIntentRepository.save(
                        paymentIntent
                );

        // ========================================================
        // CREATE PAYMENT TRANSACTION
        // ========================================================

        Transaction transaction =
                new Transaction();

        transaction.setAmount(
                savedPayment.getAmount()
        );

        transaction.setCurrency(
                savedPayment.getCurrency()
        );

        transaction.setType(
                TransactionType.PAYMENT
        );

        transaction.setStatus(
                PaymentStatus.CAPTURED
        );

        transaction.setCreatedAt(
                LocalDateTime.now()
        );

        transaction.setPaymentIntent(
                savedPayment
        );

        transactionRepository.save(
                transaction
        );

        // ========================================================
        // CREATE WEBHOOK EVENT
        // ========================================================

        webhookEventService.createWebhookEvent(
                savedPayment,
                PaymentEventType.PAYMENT_CAPTURED
        );

        // ========================================================
        // CREATE AUDIT LOG
        // ========================================================

        auditLogService.createAuditLog(
                savedPayment.getId(),
                "PAYMENT_CAPTURED",
                "Payment intent captured"
        );

        return mapToResponse(
                savedPayment
        );
    }

    // ============================================================
    // REFUND PAYMENT INTENT
    // CAPTURED → REFUNDED
    // ============================================================

    @Transactional
    public PaymentIntentResponse refundPaymentIntent(Long id) {

        PaymentIntent paymentIntent =
                paymentIntentRepository.findById(id)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment intent not found with id: "
                                                + id
                                )
                        );

        // ========================================================
        // VALIDATE STATE TRANSITION
        // ========================================================

        if (paymentIntent.getStatus()
                != PaymentStatus.CAPTURED) {

            throw new RuntimeException(
                    "Payment can only be refunded from CAPTURED status"
            );
        }

        // ========================================================
        // CHANGE PAYMENT STATUS
        // ========================================================

        paymentIntent.setStatus(
                PaymentStatus.REFUNDED
        );

        // ========================================================
        // SAVE PAYMENT
        // ========================================================

        PaymentIntent savedPayment =
                paymentIntentRepository.save(
                        paymentIntent
                );

        // ========================================================
        // CREATE REFUND TRANSACTION
        // ========================================================

        Transaction refundTransaction =
                new Transaction();

        refundTransaction.setAmount(
                savedPayment.getAmount()
        );

        refundTransaction.setCurrency(
                savedPayment.getCurrency()
        );

        refundTransaction.setType(
                TransactionType.REFUND
        );

        refundTransaction.setStatus(
                PaymentStatus.REFUNDED
        );

        refundTransaction.setCreatedAt(
                LocalDateTime.now()
        );

        refundTransaction.setPaymentIntent(
                savedPayment
        );

        transactionRepository.save(
                refundTransaction
        );

        // ========================================================
        // CREATE WEBHOOK EVENT
        // ========================================================

        webhookEventService.createWebhookEvent(
                savedPayment,
                PaymentEventType.PAYMENT_REFUNDED
        );

        // ========================================================
        // CREATE AUDIT LOG
        // ========================================================

        auditLogService.createAuditLog(
                savedPayment.getId(),
                "PAYMENT_REFUNDED",
                "Payment intent refunded"
        );

        return mapToResponse(
                savedPayment
        );
    }

    // ============================================================
    // ENTITY → RESPONSE DTO
    // ============================================================

    private PaymentIntentResponse mapToResponse(
            PaymentIntent paymentIntent) {

        PaymentIntentResponse response =
                new PaymentIntentResponse();

        response.setId(
                paymentIntent.getId()
        );

        response.setAmount(
                paymentIntent.getAmount()
        );

        response.setCurrency(
                paymentIntent.getCurrency()
        );

        response.setStatus(
                paymentIntent.getStatus()
        );

        response.setCreatedAt(
                paymentIntent.getCreatedAt()
        );

        response.setMerchantName(
                paymentIntent
                        .getMerchant()
                        .getMerchantName()
        );

        return response;
    }
}