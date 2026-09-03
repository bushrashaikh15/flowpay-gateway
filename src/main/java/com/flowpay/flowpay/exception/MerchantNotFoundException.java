package com.flowpay.flowpay.exception;

public class MerchantNotFoundException extends RuntimeException {

    public MerchantNotFoundException(String message) {
        super(message);
    }
}