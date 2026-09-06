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
import com.flowpay.flowpay.exception.UnauthorizedResourceException;
import com.flowpay.flowpay.repository.MerchantRepository;
import com.flowpay.flowpay.repository.PaymentIntentRepository;
import com.flowpay.flowpay.repository.TransactionRepository;
import com.flowpay.flowpay.specification.PaymentIntentSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PaymentIntentService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentIntentService.class);

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
            String idempotencyKey,
            Merchant authenticatedMerchant) {

        logger.info(
                "Creating payment intent for merchant: {} with amount: {} {}",
                authenticatedMerchant.getId(),
                request.getAmount(),
                request.getCurrency()
        );

        // ========================================================
        // VERIFY MERCHANT OWNERSHIP
        // ========================================================

        if (!authenticatedMerchant.getId()
                .equals(request.getMerchantId())) {

            logger.warn(
                    "Merchant {} attempted to create payment for merchant {}",
                    authenticatedMerchant.getId(),
                    request.getMerchantId()
            );

            throw new RuntimeException(
                    "You can only create payment intents for your own merchant account"
            );
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {

            logger.warn(
                    "Payment intent creation failed: Idempotency-Key is missing"
            );

            throw new RuntimeException(
                    "Idempotency-Key header is required"
            );
        }

        String redisKey =
                "idempotency:"
                        + authenticatedMerchant.getId()
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

            logger.info(
                    "Duplicate payment request detected for merchant: {}",
                    authenticatedMerchant.getId()
            );

            String cachedResponse =
                    redisTemplate
                            .opsForValue()
                            .get(redisKey);

            if ("PROCESSING".equals(cachedResponse)) {

                logger.warn(
                        "Payment request is already being processed for merchant: {}",
                        authenticatedMerchant.getId()
                );

                throw new RuntimeException(
                        "Payment request is already being processed"
                );
            }

            if (cachedResponse != null) {

                try {

                    logger.info(
                            "Returning cached payment response for merchant: {}",
                            authenticatedMerchant.getId()
                    );

                    return objectMapper.readValue(
                            cachedResponse,
                            PaymentIntentResponse.class
                    );

                } catch (JsonProcessingException e) {

                    logger.error(
                            "Failed to read cached payment response",
                            e
                    );

                    throw new RuntimeException(
                            "Failed to read cached payment response",
                            e
                    );
                }
            }
        }

        try {

            // ====================================================
            // FIND AUTHENTICATED MERCHANT
            // ====================================================

            Merchant merchant =
                    merchantRepository.findById(
                            authenticatedMerchant.getId()
                    ).orElseThrow(() -> {

                        logger.warn(
                                "Authenticated merchant not found: {}",
                                authenticatedMerchant.getId()
                        );

                        return new MerchantNotFoundException(
                                "Merchant not found with id: "
                                        + authenticatedMerchant.getId()
                        );
                    });

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

            logger.info(
                    "Payment intent created successfully: {} for merchant: {}",
                    savedPayment.getId(),
                    merchant.getId()
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

            logger.info(
                    "Payment intent response cached successfully in Redis: {}",
                    savedPayment.getId()
            );

            return response;

        } catch (MerchantNotFoundException e) {

            logger.warn(
                    "Payment intent creation failed because merchant was not found: {}",
                    authenticatedMerchant.getId()
            );

            redisTemplate.delete(redisKey);

            throw e;

        } catch (Exception e) {

            logger.error(
                    "Unexpected error while creating payment intent for merchant: {}",
                    authenticatedMerchant.getId(),
                    e
            );

            redisTemplate.delete(redisKey);

            throw new RuntimeException(
                    "Failed to create payment intent",
                    e
            );
        }
    }

    // ============================================================
    // GET ALL PAYMENT INTENTS
    // WITH FILTERING + PAGINATION + OWNERSHIP
    // ============================================================

    public Page<PaymentIntentResponse> getAllPaymentIntents(
            Pageable pageable,
            PaymentStatus status,
            String currency,
            Double minAmount,
            Double maxAmount,
            Merchant authenticatedMerchant) {

        logger.info(
                "Fetching payment intents for merchant: {} - page: {}, size: {}, status: {}, currency: {}, minAmount: {}, maxAmount: {}",
                authenticatedMerchant.getId(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                status,
                currency,
                minAmount,
                maxAmount
        );

        Specification<PaymentIntent> specification =
                (root, query, criteriaBuilder) -> null;

        // ========================================================
        // MERCHANT OWNERSHIP FILTER
        // ========================================================

        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(
                                root.get("merchant").get("id"),
                                authenticatedMerchant.getId()
                        )
        );

        // ========================================================
        // STATUS FILTER
        // ========================================================

        if (status != null) {

            specification = specification.and(
                    PaymentIntentSpecification.hasStatus(status)
            );
        }

        // ========================================================
        // CURRENCY FILTER
        // ========================================================

        if (currency != null && !currency.isBlank()) {

            specification = specification.and(
                    PaymentIntentSpecification.hasCurrency(currency)
            );
        }

        // ========================================================
        // MINIMUM AMOUNT FILTER
        // ========================================================

        if (minAmount != null) {

            specification = specification.and(
                    PaymentIntentSpecification.amountGreaterThanOrEqualTo(
                            minAmount
                    )
            );
        }

        // ========================================================
        // MAXIMUM AMOUNT FILTER
        // ========================================================

        if (maxAmount != null) {

            specification = specification.and(
                    PaymentIntentSpecification.amountLessThanOrEqualTo(
                            maxAmount
                    )
            );
        }

        // ========================================================
        // APPLY FILTERS + PAGINATION
        // ========================================================

        return paymentIntentRepository
                .findAll(
                        specification,
                        pageable
                )
                .map(this::mapToResponse);
    }

    // ============================================================
    // GET PAYMENT INTENT BY ID
    // ============================================================

    public PaymentIntentResponse getPaymentIntentById(
            Long id,
            Merchant authenticatedMerchant) {

        logger.info(
                "Fetching payment intent: {} for merchant: {}",
                id,
                authenticatedMerchant.getId()
        );

        PaymentIntent paymentIntent =
                findOwnedPaymentIntent(
                        id,
                        authenticatedMerchant
                );

        return mapToResponse(
                paymentIntent
        );
    }

    // ============================================================
    // AUTHORIZE
    // CREATED → AUTHORIZED
    // ============================================================

    @Transactional
    public PaymentIntentResponse authorizePaymentIntent(
            Long id,
            Merchant authenticatedMerchant) {

        logger.info(
                "Authorizing payment intent: {} for merchant: {}",
                id,
                authenticatedMerchant.getId()
        );

        PaymentIntent paymentIntent =
                findOwnedPaymentIntent(
                        id,
                        authenticatedMerchant
                );

        // ========================================================
        // VALIDATE STATE TRANSITION
        // ========================================================

        if (paymentIntent.getStatus()
                != PaymentStatus.CREATED) {

            logger.warn(
                    "Payment {} cannot be authorized from status: {}",
                    id,
                    paymentIntent.getStatus()
            );

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

        logger.info(
                "Payment intent authorized successfully: {}",
                id
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
    // CAPTURE
    // AUTHORIZED → CAPTURED
    // ============================================================

    @Transactional
    public PaymentIntentResponse capturePaymentIntent(
            Long id,
            Merchant authenticatedMerchant) {

        logger.info(
                "Capturing payment intent: {} for merchant: {}",
                id,
                authenticatedMerchant.getId()
        );

        PaymentIntent paymentIntent =
                findOwnedPaymentIntent(
                        id,
                        authenticatedMerchant
                );

        // ========================================================
        // VALIDATE STATE TRANSITION
        // ========================================================

        if (paymentIntent.getStatus()
                != PaymentStatus.AUTHORIZED) {

            logger.warn(
                    "Payment {} cannot be captured from status: {}",
                    id,
                    paymentIntent.getStatus()
            );

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

        logger.info(
                "Payment intent captured successfully: {}",
                id
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

        logger.info(
                "Payment transaction created for payment intent: {}",
                id
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
    // REFUND
    // CAPTURED → REFUNDED
    // ============================================================

    @Transactional
    public PaymentIntentResponse refundPaymentIntent(
            Long id,
            Merchant authenticatedMerchant) {

        logger.info(
                "Refunding payment intent: {} for merchant: {}",
                id,
                authenticatedMerchant.getId()
        );

        PaymentIntent paymentIntent =
                findOwnedPaymentIntent(
                        id,
                        authenticatedMerchant
                );

        // ========================================================
        // VALIDATE STATE TRANSITION
        // ========================================================

        if (paymentIntent.getStatus()
                != PaymentStatus.CAPTURED) {

            logger.warn(
                    "Payment {} cannot be refunded from status: {}",
                    id,
                    paymentIntent.getStatus()
            );

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

        logger.info(
                "Payment intent refunded successfully: {}",
                id
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

        logger.info(
                "Refund transaction created for payment intent: {}",
                id
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
    // FIND PAYMENT OWNED BY AUTHENTICATED MERCHANT
    // ============================================================

    private PaymentIntent findOwnedPaymentIntent(
            Long id,
            Merchant authenticatedMerchant) {

        PaymentIntent paymentIntent =
                paymentIntentRepository.findById(id)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Payment intent not found: {}",
                                    id
                            );

                            return new PaymentNotFoundException(
                                    "Payment intent not found with id: "
                                            + id
                            );
                        });

        // ========================================================
        // OWNERSHIP CHECK
        // ========================================================

        if (paymentIntent.getMerchant() == null ||
                !paymentIntent
                        .getMerchant()
                        .getId()
                        .equals(authenticatedMerchant.getId())) {

            logger.warn(
                    "Merchant {} attempted to access payment intent {} owned by another merchant",
                    authenticatedMerchant.getId(),
                    id
            );

            throw new UnauthorizedResourceException(
                    "You are not authorized to access this payment intent"
            );
        }

        return paymentIntent;
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