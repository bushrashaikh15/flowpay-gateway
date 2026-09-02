package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.TransactionResponse;
import com.flowpay.flowpay.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    // ============================================================
    // GET ALL TRANSACTIONS
    // ============================================================

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {

        return transactionService.getAllTransactions();
    }

    // ============================================================
    // GET TRANSACTIONS BY PAYMENT INTENT ID
    // ============================================================

    @GetMapping("/payment/{paymentIntentId}")
    public List<TransactionResponse>
    getTransactionsByPaymentIntentId(
            @PathVariable Long paymentIntentId) {

        return transactionService
                .getTransactionsByPaymentIntentId(
                        paymentIntentId
                );
    }
}