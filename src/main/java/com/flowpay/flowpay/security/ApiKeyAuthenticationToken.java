package com.flowpay.flowpay.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String apiKey;
    private final Object principal;

    // Used when the request is not yet authenticated
    public ApiKeyAuthenticationToken(String apiKey) {
        super(Collections.emptyList());

        this.apiKey = apiKey;
        this.principal = null;

        setAuthenticated(false);
    }

    // Used after successful authentication
    public ApiKeyAuthenticationToken(
            Object principal,
            String apiKey) {

        super(Collections.emptyList());

        this.principal = principal;
        this.apiKey = apiKey;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}