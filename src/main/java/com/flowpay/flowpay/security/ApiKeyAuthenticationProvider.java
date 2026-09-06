package com.flowpay.flowpay.security;

import com.flowpay.flowpay.entity.Merchant;
import com.flowpay.flowpay.repository.MerchantRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ApiKeyAuthenticationProvider
        implements AuthenticationProvider {

    private final MerchantRepository merchantRepository;

    public ApiKeyAuthenticationProvider(
            MerchantRepository merchantRepository) {

        this.merchantRepository = merchantRepository;
    }

    @Override
    public Authentication authenticate(
            Authentication authentication)
            throws AuthenticationException {

        String apiKey =
                (String) authentication.getCredentials();

        if (apiKey == null || apiKey.isBlank()) {
            throw new BadCredentialsException(
                    "API key is missing"
            );
        }

        Merchant merchant =
                merchantRepository.findByApiKey(apiKey)
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        "Invalid API key"
                                )
                        );

        if (!merchant.isActive()) {
            throw new BadCredentialsException(
                    "Merchant account is inactive"
            );
        }

        return new ApiKeyAuthenticationToken(
                merchant,
                apiKey
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {

        return ApiKeyAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}