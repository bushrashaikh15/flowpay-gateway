package com.flowpay.flowpay.service;

import com.flowpay.flowpay.dto.TransactionResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.entity.Transaction;
import com.flowpay.flowpay.exception.UnauthorizedResourceException;
import com.flowpay.flowpay.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(
            TransactionRepository transactionRepository) {

        this.transactionRepository = transactionRepository;
    }

    // ============================================================
    // GET ALL TRANSACTIONS
    // ============================================================

    public List<TransactionResponse> getAllTransactions(
            Merchant authenticatedMerchant) {

        return transactionRepository.findAll()
                .stream()
                .filter(transaction ->
                        transaction.getPaymentIntent() != null
                                && transaction.getPaymentIntent()
                                .getMerchant() != null
                                && transaction.getPaymentIntent()
                                .getMerchant()
                                .getId()
                                .equals(
                                        authenticatedMerchant.getId()
                                )
                )
                .map(this::mapToResponse)
                .toList();
    }

    // ============================================================
    // GET TRANSACTIONS BY PAYMENT INTENT ID
    // ============================================================

    public List<TransactionResponse>
    getTransactionsByPaymentIntentId(
            Long paymentIntentId,
            Merchant authenticatedMerchant) {

        List<Transaction> transactions =
                transactionRepository
                        .findByPaymentIntentId(
                                paymentIntentId
                        );

        if (!transactions.isEmpty()) {

            Transaction transaction =
                    transactions.get(0);

            if (transaction.getPaymentIntent() == null
                    || transaction.getPaymentIntent().getMerchant() == null
                    || !transaction
                    .getPaymentIntent()
                    .getMerchant()
                    .getId()
                    .equals(
                            authenticatedMerchant.getId()
                    )) {

                throw new UnauthorizedResourceException(
                        "You are not authorized to access these transactions"
                );
            }
        }

        return transactions
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ============================================================
    // ENTITY → RESPONSE DTO
    // ============================================================

    private TransactionResponse mapToResponse(
            Transaction transaction) {

        TransactionResponse response =
                new TransactionResponse();

        response.setId(
                transaction.getId()
        );

        response.setAmount(
                transaction.getAmount()
        );

        response.setCurrency(
                transaction.getCurrency()
        );

        response.setType(
                transaction.getType()
        );

        response.setStatus(
                transaction.getStatus()
        );

        response.setCreatedAt(
                transaction.getCreatedAt()
        );

        response.setPaymentIntentId(
                transaction
                        .getPaymentIntent()
                        .getId()
        );

        return response;
    }
}