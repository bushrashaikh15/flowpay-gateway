package com.flowpay.flowpay.dto;

public class MerchantPublicResponse {

    private Long id;
    private String merchantName;
    private String email;
    private boolean active;

    public MerchantPublicResponse() {
    }

    public MerchantPublicResponse(
            Long id,
            String merchantName,
            String email,
            boolean active) {

        this.id = id;
        this.merchantName = merchantName;
        this.email = email;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}