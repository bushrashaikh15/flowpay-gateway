package com.flowpay.flowpay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class MerchantRequest {

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    public MerchantRequest() {
    }

    public MerchantRequest(String merchantName, String email) {
        this.merchantName = merchantName;
        this.email = email;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}