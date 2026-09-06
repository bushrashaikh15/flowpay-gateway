package com.flowpay.flowpay.exception;

public class UnauthorizedResourceException extends RuntimeException {

    public UnauthorizedResourceException(String message) {
        super(message);
    }
}