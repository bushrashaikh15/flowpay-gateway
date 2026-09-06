package com.flowpay.flowpay.controller;

import com.flowpay.flowpay.dto.MerchantRequest;
import com.flowpay.flowpay.dto.MerchantResponse;
import com.flowpay.flowpay.dto.MerchantPublicResponse;
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

    @PostMapping
    public MerchantResponse createMerchant(
            @Valid @RequestBody MerchantRequest request) {

        return merchantService.createMerchant(request);
    }

    @GetMapping
    public List<MerchantPublicResponse> getAllMerchants() {

        return merchantService.getAllMerchants()
                .stream()
                .map(merchant -> new MerchantPublicResponse(
                        merchant.getId(),
                        merchant.getMerchantName(),
                        merchant.getEmail(),
                        merchant.isActive()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public MerchantPublicResponse getMerchantById(
            @PathVariable Long id) {

        MerchantResponse merchant =
                merchantService.getMerchantById(id);

        return new MerchantPublicResponse(
                merchant.getId(),
                merchant.getMerchantName(),
                merchant.getEmail(),
                merchant.isActive()
        );
    }

    @PutMapping("/{id}")
    public MerchantPublicResponse updateMerchant(
            @PathVariable Long id,
            @Valid @RequestBody MerchantRequest request) {

        MerchantResponse merchant =
                merchantService.updateMerchant(id, request);

        return new MerchantPublicResponse(
                merchant.getId(),
                merchant.getMerchantName(),
                merchant.getEmail(),
                merchant.isActive()
        );
    }

    @DeleteMapping("/{id}")
    public String deleteMerchant(
            @PathVariable Long id) {

        merchantService.deleteMerchant(id);

        return "Merchant deleted successfully";
    }
}