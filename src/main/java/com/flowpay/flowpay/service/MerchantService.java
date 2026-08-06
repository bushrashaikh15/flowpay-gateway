package com.flowpay.flowpay.service;

import com.flowpay.flowpay.dto.MerchantRequest;
import com.flowpay.flowpay.dto.MerchantResponse;
import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    // Create Merchant
    public MerchantResponse createMerchant(MerchantRequest request) {

        Merchant merchant = new Merchant();

        merchant.setMerchantName(request.getMerchantName());
        merchant.setEmail(request.getEmail());

        merchant.setApiKey("API_" + System.currentTimeMillis());
        merchant.setActive(true);

        Merchant savedMerchant = merchantRepository.save(merchant);

        return convertToResponse(savedMerchant);
    }

    // Get All Merchants
    public List<MerchantResponse> getAllMerchants() {

        return merchantRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get Merchant By ID
    public MerchantResponse getMerchantById(Long id) {

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));

        return convertToResponse(merchant);
    }

    // Update Merchant
    public MerchantResponse updateMerchant(Long id, MerchantRequest request) {

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));

        merchant.setMerchantName(request.getMerchantName());
        merchant.setEmail(request.getEmail());

        Merchant updatedMerchant = merchantRepository.save(merchant);

        return convertToResponse(updatedMerchant);
    }

    // Delete Merchant
    public void deleteMerchant(Long id) {

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));

        merchantRepository.delete(merchant);
    }

    // Convert Entity -> Response DTO
    private MerchantResponse convertToResponse(Merchant merchant) {

        MerchantResponse response = new MerchantResponse();

        response.setId(merchant.getId());
        response.setMerchantName(merchant.getMerchantName());
        response.setEmail(merchant.getEmail());
        response.setActive(merchant.isActive());

        return response;
    }
}