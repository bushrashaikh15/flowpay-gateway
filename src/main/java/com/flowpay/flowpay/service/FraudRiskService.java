package com.flowpay.flowpay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowpay.flowpay.dto.FraudInsightResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.exception.PaymentNotFoundException;
import com.flowpay.flowpay.exception.UnauthorizedResourceException;
import com.flowpay.flowpay.repository.PaymentIntentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudRiskService {

    private static final Logger logger =
            LoggerFactory.getLogger(FraudRiskService.class);

    private final PaymentIntentRepository paymentIntentRepository;

    private final ObjectMapper objectMapper;

    private final RestClient restClient;

    private final String ollamaModel;


    // =============================================================
    // CONSTRUCTOR
    // =============================================================

    public FraudRiskService(
            PaymentIntentRepository paymentIntentRepository,
            ObjectMapper objectMapper,
            @Value("${ollama.model:llama3}")
            String ollamaModel,
            @Value("${ollama.base-url:http://localhost:11434}")
            String ollamaBaseUrl) {

        this.paymentIntentRepository =
                paymentIntentRepository;

        this.objectMapper = objectMapper;

        this.ollamaModel = ollamaModel;

        this.restClient =
                RestClient.builder()
                        .baseUrl(ollamaBaseUrl)
                        .build();
    }


    // =============================================================
    // ANALYZE PAYMENT
    // =============================================================

    public FraudInsightResponse analyzePayment(
            Long paymentIntentId,
            Merchant authenticatedMerchant) {

        PaymentIntent paymentIntent =
                paymentIntentRepository
                        .findById(paymentIntentId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment intent not found with id: "
                                                + paymentIntentId
                                )
                        );


        // =========================================================
        // MERCHANT OWNERSHIP
        // =========================================================

        if (paymentIntent.getMerchant() == null
                || !paymentIntent
                .getMerchant()
                .getId()
                .equals(
                        authenticatedMerchant.getId()
                )) {

            throw new UnauthorizedResourceException(
                    "You are not authorized to analyze this payment"
            );
        }


        // =========================================================
        // FETCH RECENT PAYMENTS FOR THIS MERCHANT
        // =========================================================

        Specification<PaymentIntent> merchantSpecification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(
                                root.get("merchant").get("id"),
                                authenticatedMerchant.getId()
                        );

        List<PaymentIntent> recentPayments =
                paymentIntentRepository
                        .findAll(
                                merchantSpecification,
                                PageRequest.of(
                                        0,
                                        50,
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "createdAt"
                                        )
                                )
                        )
                        .getContent();


        // =========================================================
        // CALCULATE FRAUD RISK
        // =========================================================

        int riskScore = 0;

        List<String> riskSignals =
                new ArrayList<>();


        // =========================================================
        // RULE 1 — HIGH PAYMENT AMOUNT
        // =========================================================

        BigDecimal amount =
                paymentIntent.getAmount();

        if (amount.compareTo(
                new BigDecimal("100000")
        ) >= 0) {

            riskScore += 30;

            riskSignals.add(
                    "Very high transaction amount"
            );

        } else if (amount.compareTo(
                new BigDecimal("50000")
        ) >= 0) {

            riskScore += 15;

            riskSignals.add(
                    "High transaction amount"
            );
        }


        // =========================================================
        // RULE 2 — ABOVE MERCHANT AVERAGE
        // =========================================================

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        int amountCount = 0;

        for (PaymentIntent payment :
                recentPayments) {

            if (payment.getAmount() != null) {

                totalAmount =
                        totalAmount.add(
                                payment.getAmount()
                        );

                amountCount++;
            }
        }

        if (amountCount > 1) {

            BigDecimal averageAmount =
                    totalAmount.divide(
                            BigDecimal.valueOf(
                                    amountCount
                            ),
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            BigDecimal comparisonAmount =
                    averageAmount.multiply(
                            new BigDecimal("3")
                    );

            if (amount.compareTo(
                    comparisonAmount
            ) > 0) {

                riskScore += 25;

                riskSignals.add(
                        "Transaction amount is significantly above the merchant's recent average"
                );
            }
        }


        // =========================================================
        // RULE 3 — RAPID PAYMENT ACTIVITY
        // =========================================================

        LocalDateTime tenMinutesAgo =
                LocalDateTime.now()
                        .minusMinutes(10);

        int recentAttemptCount = 0;

        for (PaymentIntent payment :
                recentPayments) {

            if (payment.getCreatedAt() != null
                    && payment
                    .getCreatedAt()
                    .isAfter(tenMinutesAgo)) {

                recentAttemptCount++;
            }
        }

        if (recentAttemptCount >= 5) {

            riskScore += 25;

            riskSignals.add(
                    "Multiple payment attempts detected within a short period"
            );

        } else if (recentAttemptCount >= 3) {

            riskScore += 15;

            riskSignals.add(
                    "Elevated payment frequency detected"
            );
        }


        // =========================================================
        // RULE 4 — REPEATED PAYMENT AMOUNT
        // =========================================================

        long sameAmountCount =
                recentPayments
                        .stream()
                        .filter(payment ->
                                payment.getAmount() != null
                                        && payment
                                        .getAmount()
                                        .compareTo(amount)
                                        == 0
                        )
                        .count();

        if (sameAmountCount >= 3) {

            riskScore += 10;

            riskSignals.add(
                    "Repeated transactions with the same amount detected"
            );
        }


        // =========================================================
        // LIMIT SCORE
        // =========================================================

        riskScore =
                Math.min(
                        riskScore,
                        100
                );


        // =========================================================
        // DETERMINE RISK LEVEL
        // =========================================================

        String riskLevel;

        if (riskScore >= 60) {

            riskLevel = "HIGH";

        } else if (riskScore >= 30) {

            riskLevel = "MEDIUM";

        } else {

            riskLevel = "LOW";
        }


        // =========================================================
        // GENERATE AI EXPLANATION
        // =========================================================

        String aiExplanation =
                generateAiExplanation(
                        paymentIntent,
                        riskScore,
                        riskLevel,
                        riskSignals
                );


        // =========================================================
        // RESPONSE
        // =========================================================

        return new FraudInsightResponse(
                paymentIntent.getId(),
                riskScore,
                riskLevel,
                riskSignals,
                aiExplanation,
                "Fraud score is an automated risk signal and should not be treated as a final fraud determination."
        );
    }


    // =============================================================
    // OLLAMA AI EXPLANATION
    // =============================================================

    private String generateAiExplanation(
            PaymentIntent paymentIntent,
            int riskScore,
            String riskLevel,
            List<String> riskSignals) {

        try {

            // -----------------------------------------------------
            // BUILD PROMPT
            // -----------------------------------------------------

            String prompt = """
                    You are a fraud-risk explanation assistant for a payment gateway called FlowPay.

                    Your job is ONLY to explain the risk assessment generated by FlowPay's backend rules.

                    You are NOT the final fraud decision-maker.

                    Do NOT claim that a payment is definitely fraudulent.

                    Do NOT invent transaction information.

                    Do NOT change the backend-generated risk score.

                    Payment details:

                    Payment Intent ID: %d
                    Payment amount: %s
                    Currency: %s
                    Payment status: %s
                    Risk score: %d/100
                    Risk level: %s

                    Detected risk signals:
                    %s

                    Explain the risk assessment in 2 to 4 concise sentences.

                    Explain why the detected signals may require additional review.

                    Keep the explanation professional and suitable for a merchant dashboard.

                    If there are no risk signals, explain that the backend rules detected no significant risk indicators.

                    Do not make a final fraud determination.
                    """.formatted(
                    paymentIntent.getId(),
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency(),
                    paymentIntent.getStatus(),
                    riskScore,
                    riskLevel,
                    riskSignals.isEmpty()
                            ? "No significant risk signals detected."
                            : String.join(
                            ", ",
                            riskSignals
                    )
            );


            // -----------------------------------------------------
            // BUILD OLLAMA JSON REQUEST
            // -----------------------------------------------------

            ObjectNode request =
                    objectMapper.createObjectNode();

            request.put(
                    "model",
                    ollamaModel
            );

            request.put(
                    "prompt",
                    prompt
            );

            request.put(
                    "stream",
                    false
            );


            // -----------------------------------------------------
            // CONVERT JSON TO STRING
            // -----------------------------------------------------

            String requestBody =
                    objectMapper.writeValueAsString(
                            request
                    );


            // -----------------------------------------------------
            // LOG REQUEST
            // -----------------------------------------------------

            logger.info(
                    "Sending fraud explanation request to Ollama using model: {}",
                    ollamaModel
            );

            logger.debug(
                    "Ollama request body: {}",
                    requestBody
            );


            // -----------------------------------------------------
            // CALL OLLAMA
            // -----------------------------------------------------

            /*
             * IMPORTANT:
             *
             * We receive the Ollama response as String instead of
             * JsonNode because Spring Boot 4 uses Jackson 3 internally.
             *
             * Using .body(JsonNode.class) caused:
             *
             * HttpMessageConversionException:
             * Cannot construct instance of JsonNode
             *
             * So we receive raw JSON and parse it manually below.
             */

            String responseBody =
                    restClient
                            .post()
                            .uri("/api/generate")
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .body(requestBody)
                            .retrieve()
                            .body(String.class);


            // -----------------------------------------------------
            // CHECK RAW RESPONSE
            // -----------------------------------------------------

            if (responseBody == null
                    || responseBody.isBlank()) {

                logger.warn(
                        "Ollama returned an empty response"
                );

                return fallbackExplanation(
                        riskLevel,
                        riskSignals
                );
            }


            logger.debug(
                    "Ollama raw response: {}",
                    responseBody
            );


            // -----------------------------------------------------
            // PARSE OLLAMA JSON RESPONSE
            // -----------------------------------------------------

            JsonNode response;

            try {

                response =
                        objectMapper.readTree(
                                responseBody
                        );

            } catch (Exception parseException) {

                logger.warn(
                        "Could not parse Ollama response: {}",
                        responseBody,
                        parseException
                );

                return fallbackExplanation(
                        riskLevel,
                        riskSignals
                );
            }


            // -----------------------------------------------------
            // EXTRACT AI RESPONSE
            // -----------------------------------------------------

            String explanation =
                    response
                            .path("response")
                            .asText("");


            // -----------------------------------------------------
            // CHECK AI RESPONSE
            // -----------------------------------------------------

            if (explanation == null
                    || explanation.isBlank()) {

                logger.warn(
                        "Ollama response did not contain generated text: {}",
                        response
                );

                return fallbackExplanation(
                        riskLevel,
                        riskSignals
                );
            }


            // -----------------------------------------------------
            // SUCCESS
            // -----------------------------------------------------

            logger.info(
                    "AI fraud explanation generated successfully using Ollama"
            );

            return explanation.trim();


        } catch (RestClientException ex) {

            logger.warn(
                    "Ollama fraud explanation request failed",
                    ex
            );

            return fallbackExplanation(
                    riskLevel,
                    riskSignals
            );

        } catch (Exception ex) {

            logger.warn(
                    "Unexpected error while generating AI fraud explanation",
                    ex
            );

            return fallbackExplanation(
                    riskLevel,
                    riskSignals
            );
        }
    }


    // =============================================================
    // FALLBACK
    // =============================================================

    private String fallbackExplanation(
            String riskLevel,
            List<String> riskSignals) {

        if (riskSignals.isEmpty()) {

            return "No significant risk signals were detected by the current rule-based assessment.";
        }

        return "The payment is classified as "
                + riskLevel
                + " risk because "
                + String.join(
                ", ",
                riskSignals
        )
                + ".";
    }
}