package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.MerchantRequest;
import com.flowpay.flowpay.dto.MerchantResponse;
import com.flowpay.flowpay.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    // Create Merchant
    @PostMapping
    public MerchantResponse createMerchant(@Valid @RequestBody MerchantRequest request) {
        return merchantService.createMerchant(request);
    }

    // Get All Merchants
    @GetMapping
    public List<MerchantResponse> getAllMerchants() {
        return merchantService.getAllMerchants();
    }

    // Get Merchant By ID
    @GetMapping("/{id}")
    public MerchantResponse getMerchantById(@PathVariable Long id) {
        return merchantService.getMerchantById(id);
    }

    // Update Merchant
    @PutMapping("/{id}")
    public MerchantResponse updateMerchant(@PathVariable Long id,
                                           @Valid @RequestBody MerchantRequest request) {
        return merchantService.updateMerchant(id, request);
    }

    // Delete Merchant
    @DeleteMapping("/{id}")
    public String deleteMerchant(@PathVariable Long id) {

        merchantService.deleteMerchant(id);

        return "Merchant deleted successfully";
    }
}