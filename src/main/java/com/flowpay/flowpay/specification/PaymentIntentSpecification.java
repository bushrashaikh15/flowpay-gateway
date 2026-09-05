package com.flowpay.flowpay.specification;

import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

public class PaymentIntentSpecification {

    private PaymentIntentSpecification() {
    }

    public static Specification<PaymentIntent> hasStatus(
            PaymentStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<PaymentIntent> hasCurrency(
            String currency) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.upper(
                                root.get("currency")
                        ),
                        currency.toUpperCase()
                );
    }

    public static Specification<PaymentIntent> amountGreaterThanOrEqualTo(
            Double minAmount) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("amount"),
                        minAmount
                );
    }

    public static Specification<PaymentIntent> amountLessThanOrEqualTo(
            Double maxAmount) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("amount"),
                        maxAmount
                );
    }
}