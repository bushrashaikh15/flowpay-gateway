package com.flowpay.flowpay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowpay.flowpay.dto.AnalyticsChatRequest;
import com.flowpay.flowpay.dto.AnalyticsChatResponse;
import com.flowpay.flowpay.dto.AnalyticsChatResponse.AnalyticsSummary;
import com.flowpay.flowpay.dto.MonthlyPaymentReportResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.repository.PaymentIntentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class MerchantAnalyticsService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    MerchantAnalyticsService.class
            );

    private final PaymentIntentRepository paymentIntentRepository;

    private final ObjectMapper objectMapper;

    private final RestClient restClient;

    private final String ollamaModel;


    public MerchantAnalyticsService(
            PaymentIntentRepository paymentIntentRepository,
            ObjectMapper objectMapper,
            @Value("${ollama.base-url:http://localhost:11434}")
            String ollamaBaseUrl,
            @Value("${ollama.model:llama3}")
            String ollamaModel) {

        this.paymentIntentRepository =
                paymentIntentRepository;

        this.objectMapper =
                objectMapper;

        this.ollamaModel =
                ollamaModel;

        this.restClient =
                RestClient
                        .builder()
                        .baseUrl(ollamaBaseUrl)
                        .build();
    }


    /*
     * =========================================================
     * EXISTING AI ANALYTICS CHAT
     * =========================================================
     */

    public AnalyticsChatResponse askAnalyticsQuestion(
            AnalyticsChatRequest request,
            Merchant merchant) {

        if (request == null ||
                request.getQuestion() == null ||
                request.getQuestion().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Question is required."
            );
        }

        String question =
                request.getQuestion().trim();


        /*
         * GET ONLY THIS MERCHANT'S PAYMENTS
         */

        List<PaymentIntent> payments =
                paymentIntentRepository
                        .findByMerchant_Id(
                                merchant.getId()
                        );


        /*
         * CALCULATE REAL BACKEND STATISTICS
         */

        AnalyticsSummary summary =
                buildSummary(payments);


        /*
         * SEND STRUCTURED DATA TO AI
         */

        String aiAnswer =
                generateAiAnswer(
                        question,
                        summary
                );


        return new AnalyticsChatResponse(
                question,
                aiAnswer,
                summary
        );
    }


    /*
     * =========================================================
     * MONTHLY AI PAYMENT REPORT
     * =========================================================
     */

    public MonthlyPaymentReportResponse
    generateMonthlyReport(
            int year,
            int month,
            Merchant merchant) {

        if (month < 1 || month > 12) {

            throw new IllegalArgumentException(
                    "Month must be between 1 and 12."
            );
        }


        if (year < 2000 || year > 2100) {

            throw new IllegalArgumentException(
                    "Invalid year."
            );
        }


        /*
         * =====================================================
         * GET ONLY THIS MERCHANT'S PAYMENTS
         * =====================================================
         */

        List<PaymentIntent> allPayments =
                paymentIntentRepository
                        .findByMerchant_Id(
                                merchant.getId()
                        );


        /*
         * =====================================================
         * FILTER PAYMENTS FOR REQUESTED MONTH
         * =====================================================
         */

        LocalDateTime startDate =
                LocalDateTime.of(
                        year,
                        month,
                        1,
                        0,
                        0,
                        0
                );


        LocalDateTime endDate =
                startDate.plusMonths(1);


        List<PaymentIntent> monthlyPayments =
                allPayments.stream()
                        .filter(payment ->
                                payment.getCreatedAt() != null)
                        .filter(payment -> {

                            LocalDateTime createdAt =
                                    payment.getCreatedAt();

                            return !createdAt.isBefore(
                                    startDate
                            )
                                    &&
                                    createdAt.isBefore(
                                            endDate
                                    );
                        })
                        .toList();


        /*
         * =====================================================
         * CALCULATE REAL BACKEND STATISTICS
         * =====================================================
         */

        AnalyticsSummary summary =
                buildSummary(
                        monthlyPayments
                );


        /*
         * =====================================================
         * GENERATE AI SUMMARY
         * =====================================================
         */

        String aiSummary =
                generateMonthlyAiSummary(
                        year,
                        month,
                        summary
                );


        /*
         * =====================================================
         * RETURN REPORT
         * =====================================================
         */

        return new MonthlyPaymentReportResponse(

                year,

                month,

                summary.getTotalPayments(),

                summary.getCreatedPayments(),

                summary.getAuthorizedPayments(),

                summary.getCapturedPayments(),

                summary.getRefundedPayments(),

                summary.getFailedPayments(),

                summary.getTotalPaymentAmount(),

                summary.getTotalCapturedAmount(),

                summary.getTotalRefundedAmount(),

                summary.getMostUsedCurrency(),

                aiSummary
        );
    }


    /*
     * =========================================================
     * BUILD PAYMENT STATISTICS
     * =========================================================
     */

    private AnalyticsSummary buildSummary(
            List<PaymentIntent> payments) {

        long totalPayments =
                payments.size();


        long createdPayments =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        .name()
                                        .equals("CREATED"))
                        .count();


        long authorizedPayments =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        .name()
                                        .equals("AUTHORIZED"))
                        .count();


        long capturedPayments =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        .name()
                                        .equals("CAPTURED"))
                        .count();


        long refundedPayments =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        .name()
                                        .equals("REFUNDED"))
                        .count();


        long failedPayments =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        .name()
                                        .equals("FAILED"))
                        .count();


        double totalPaymentAmount =
                payments.stream()
                        .map(PaymentIntent::getAmount)
                        .filter(amount ->
                                amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .doubleValue();


        double totalCapturedAmount =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        .name()
                                        .equals("CAPTURED"))
                        .map(PaymentIntent::getAmount)
                        .filter(amount ->
                                amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .doubleValue();


        double totalRefundedAmount =
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        .name()
                                        .equals("REFUNDED"))
                        .map(PaymentIntent::getAmount)
                        .filter(amount ->
                                amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .doubleValue();


        String mostUsedCurrency =
                payments.stream()
                        .map(PaymentIntent::getCurrency)
                        .filter(currency ->
                                currency != null)
                        .collect(
                                Collectors.groupingBy(
                                        currency ->
                                                currency,
                                        Collectors.counting()
                                )
                        )
                        .entrySet()
                        .stream()
                        .max(
                                Map.Entry.comparingByValue()
                        )
                        .map(
                                Map.Entry::getKey
                        )
                        .orElse("N/A");


        return new AnalyticsSummary(

                totalPayments,

                createdPayments,

                authorizedPayments,

                capturedPayments,

                refundedPayments,

                failedPayments,

                totalPaymentAmount,

                totalCapturedAmount,

                totalRefundedAmount,

                mostUsedCurrency
        );
    }


    /*
     * =========================================================
     * AI ANALYTICS CHAT
     * =========================================================
     */

    private String generateAiAnswer(
            String question,
            AnalyticsSummary summary) {

        String prompt = """

                You are the analytics assistant for FlowPay,
                an enterprise payment gateway simulator.

                Answer the merchant's question using ONLY the
                backend statistics provided below.

                The backend statistics are the absolute source
                of truth.

                =================================================
                CRITICAL STATUS RULES
                =================================================

                1. NEVER invent, modify, estimate, or change any
                   number provided by the backend.

                2. NEVER claim that you accessed PostgreSQL,
                   the database, or any hidden data.

                3. REFUNDED and FAILED are completely different
                   payment statuses.

                4. A REFUNDED payment must NEVER be described as
                   a FAILED payment.

                5. A FAILED payment must NEVER be inferred from
                   the existence of a refund.

                6. If Failed payments is 0, you MUST NOT say that
                   any payment failed.

                7. AUTHORIZED and CAPTURED are different statuses.

                8. An AUTHORIZED payment must NEVER be described
                   as CAPTURED unless the backend explicitly
                   reports captured payments.

                9. A CAPTURED payment must NEVER be inferred from
                   a REFUNDED payment.

                10. A REFUNDED payment must NEVER be described as
                    CAPTURED merely because the payment amount was
                    refunded.

                11. If Captured payments is 0, you MUST NOT claim
                    that any payment was captured.

                12. If Total captured amount is 0.00, you MUST NOT
                    claim that money was captured.

                13. If Refunded payments is greater than 0, you may
                    state that payments were refunded, but you MUST
                    NOT infer their previous payment status.

                14. Do not infer relationships between payment
                    statuses that are not explicitly provided.

                15. Before answering, cross-check every statement
                    about CREATED, AUTHORIZED, CAPTURED, REFUNDED,
                    and FAILED against the exact statistics below.

                16. Answer only what the merchant asked. Do not
                    add unsupported conclusions.

                17. Do not provide financial advice.

                18. Keep the answer concise, professional and useful.

                =================================================
                MERCHANT QUESTION
                =================================================

                %s

                =================================================
                BACKEND PAYMENT STATISTICS
                =================================================

                Total payments: %d

                Created payments: %d

                Authorized payments: %d

                Captured payments: %d

                Refunded payments: %d

                Failed payments: %d

                Total payment amount: %.2f

                Total captured amount: %.2f

                Total refunded amount: %.2f

                Most used currency: %s

                =================================================
                FINAL INSTRUCTION
                =================================================

                Answer the merchant's question using ONLY the
                statistics above.

                Do not infer missing information.

                Do not confuse payment statuses.

                """.formatted(

                question,

                summary.getTotalPayments(),

                summary.getCreatedPayments(),

                summary.getAuthorizedPayments(),

                summary.getCapturedPayments(),

                summary.getRefundedPayments(),

                summary.getFailedPayments(),

                summary.getTotalPaymentAmount(),

                summary.getTotalCapturedAmount(),

                summary.getTotalRefundedAmount(),

                summary.getMostUsedCurrency()
        );


        return callOllama(
                prompt,
                () -> fallbackAnswer(
                        question,
                        summary
                )
        );
    }


    /*
     * =========================================================
     * MONTHLY AI SUMMARY
     * =========================================================
     */

    private String generateMonthlyAiSummary(
            int year,
            int month,
            AnalyticsSummary summary) {

        String prompt = """

                You are FlowPay's monthly payment analytics
                assistant.

                Generate a concise, professional summary of the
                merchant's monthly payment activity.

                The backend statistics below are the ONLY source
                of truth.

                =================================================
                CRITICAL STATUS RULES
                =================================================

                1. NEVER invent, modify, estimate, or change any
                   backend number.

                2. REFUNDED and FAILED are separate payment
                   statuses.

                3. A REFUNDED payment must NEVER be described as
                   a FAILED payment.

                4. A FAILED payment must NEVER be inferred from
                   the existence of a refund.

                5. If Failed payments is 0, explicitly state that
                   there were 0 failed payments if discussing
                   payment outcomes.

                6. AUTHORIZED and CAPTURED are separate statuses.

                7. An AUTHORIZED payment must NEVER be described
                   as CAPTURED unless Captured payments is greater
                   than 0.

                8. A REFUNDED payment must NEVER be described as
                   CAPTURED merely because it was refunded.

                9. If Captured payments is 0, do not claim that
                   any payment was captured.

                10. If Total captured amount is 0.00, do not claim
                    that money was captured.

                11. If Refunded payments is greater than 0, state
                    only that the payment(s) were refunded.

                12. NEVER infer the previous status of a refunded
                    payment.

                13. Do not infer any relationship between CREATED,
                    AUTHORIZED, CAPTURED, REFUNDED, and FAILED
                    statuses unless explicitly represented by the
                    backend statistics.

                14. Do not claim access to PostgreSQL, the database,
                    or hidden payment information.

                15. Before producing the final summary, cross-check
                    every status-related statement against the exact
                    backend statistics.

                16. Do not provide financial advice.

                17. Keep the report concise and professional.

                =================================================
                REPORTING PERIOD
                =================================================

                %d-%02d

                =================================================
                BACKEND MONTHLY STATISTICS
                =================================================

                Total payments: %d

                Created payments: %d

                Authorized payments: %d

                Captured payments: %d

                Refunded payments: %d

                Failed payments: %d

                Total payment amount: %.2f

                Total captured amount: %.2f

                Total refunded amount: %.2f

                Most used currency: %s

                =================================================
                FINAL INSTRUCTION
                =================================================

                Write a short monthly summary using ONLY the
                statistics above.

                Do not invent information.

                Do not infer payment statuses.

                Do not describe refunded payments as captured
                or failed unless the backend statistics explicitly
                support those statements.

                """.formatted(

                year,

                month,

                summary.getTotalPayments(),

                summary.getCreatedPayments(),

                summary.getAuthorizedPayments(),

                summary.getCapturedPayments(),

                summary.getRefundedPayments(),

                summary.getFailedPayments(),

                summary.getTotalPaymentAmount(),

                summary.getTotalCapturedAmount(),

                summary.getTotalRefundedAmount(),

                summary.getMostUsedCurrency()
        );


        return callOllama(
                prompt,
                () -> fallbackMonthlyAnswer(
                        year,
                        month,
                        summary
                )
        );
    }


    /*
     * =========================================================
     * COMMON OLLAMA CALL
     * =========================================================
     */

    private String callOllama(
            String prompt,
            Supplier<String> fallback) {

        Map<String, Object> requestBody =
                Map.of(

                        "model",
                        ollamaModel,

                        "prompt",
                        prompt,

                        "stream",
                        false
                );


        try {

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


            if (responseBody == null ||
                    responseBody.isBlank()) {

                return fallback.get();
            }


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

                return fallback.get();
            }


            String answer =
                    response
                            .path("response")
                            .asText("");


            if (answer == null ||
                    answer.isBlank()) {

                return fallback.get();
            }


            return answer.trim();


        } catch (Exception exception) {

            logger.error(
                    "Ollama request failed.",
                    exception
            );

            return fallback.get();
        }
    }


    /*
     * =========================================================
     * NORMAL ANALYTICS FALLBACK
     * =========================================================
     */

    private String fallbackAnswer(
            String question,
            AnalyticsSummary summary) {

        return String.format(

                "Based on your FlowPay payment data, " +
                        "you have %d total payment intents, " +
                        "%d captured, %d refunded and %d failed. " +
                        "Your total payment amount is %.2f. " +
                        "Your most used currency is %s.",

                summary.getTotalPayments(),

                summary.getCapturedPayments(),

                summary.getRefundedPayments(),

                summary.getFailedPayments(),

                summary.getTotalPaymentAmount(),

                summary.getMostUsedCurrency()
        );
    }


    /*
     * =========================================================
     * MONTHLY REPORT FALLBACK
     * =========================================================
     */

    private String fallbackMonthlyAnswer(
            int year,
            int month,
            AnalyticsSummary summary) {

        return String.format(

                "For %d-%02d, FlowPay recorded %d payment(s) " +
                        "with a total payment amount of %.2f. " +
                        "%d payment(s) were captured, %d were refunded " +
                        "and %d were failed. " +
                        "The total captured amount was %.2f and the " +
                        "total refunded amount was %.2f. " +
                        "The most used currency was %s.",

                year,

                month,

                summary.getTotalPayments(),

                summary.getTotalPaymentAmount(),

                summary.getCapturedPayments(),

                summary.getRefundedPayments(),

                summary.getFailedPayments(),

                summary.getTotalCapturedAmount(),

                summary.getTotalRefundedAmount(),

                summary.getMostUsedCurrency()
        );
    }
}