package com.flowpay.flowpay.service;

import com.flowpay.flowpay.dto.PaymentIntentRequest;
import com.flowpay.flowpay.dto.PaymentIntentResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.entity.PaymentIntent;
import com.flowpay.flowpay.enums.PaymentStatus;
import com.flowpay.flowpay.repository.MerchantRepository;
import com.flowpay.flowpay.repository.PaymentIntentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentIntentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final MerchantRepository merchantRepository;

    public PaymentIntentService(PaymentIntentRepository paymentIntentRepository,
                                MerchantRepository merchantRepository) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.merchantRepository = merchantRepository;
    }

    // Create Payment Intent
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) {

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new RuntimeException("Merchant not found"));

        PaymentIntent paymentIntent = new PaymentIntent();

        paymentIntent.setAmount(request.getAmount());
        paymentIntent.setCurrency(request.getCurrency());
        paymentIntent.setStatus(PaymentStatus.CREATED);
        paymentIntent.setCreatedAt(LocalDateTime.now());
        paymentIntent.setMerchant(merchant);

        PaymentIntent savedPayment = paymentIntentRepository.save(paymentIntent);

        return mapToResponse(savedPayment);
    }

    // Get All Payment Intents
    public List<PaymentIntentResponse> getAllPaymentIntents() {

        return paymentIntentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get Payment Intent By ID
    public PaymentIntentResponse getPaymentIntentById(Long id) {

        PaymentIntent paymentIntent = paymentIntentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment Intent not found"));

        return mapToResponse(paymentIntent);
    }

    // Convert Entity to DTO
    private PaymentIntentResponse mapToResponse(PaymentIntent paymentIntent) {

        PaymentIntentResponse response = new PaymentIntentResponse();

        response.setId(paymentIntent.getId());
        response.setAmount(paymentIntent.getAmount());
        response.setCurrency(paymentIntent.getCurrency());
        response.setStatus(paymentIntent.getStatus());
        response.setCreatedAt(paymentIntent.getCreatedAt());
        response.setMerchantName(paymentIntent.getMerchant().getMerchantName());

        return response;
    }
}