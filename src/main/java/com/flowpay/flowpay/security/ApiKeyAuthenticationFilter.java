package com.flowpay.flowpay.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ApiKeyAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final AuthenticationManager authenticationManager;

    public ApiKeyAuthenticationFilter(
            AuthenticationManager authenticationManager) {

        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey =
                request.getHeader(API_KEY_HEADER);

        if (apiKey != null && !apiKey.isBlank()) {

            try {

                ApiKeyAuthenticationToken authenticationToken =
                        new ApiKeyAuthenticationToken(apiKey);

                Authentication authenticated =
                        authenticationManager.authenticate(
                                authenticationToken
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authenticated);

            } catch (Exception ex) {

                SecurityContextHolder
                        .clearContext();

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.setContentType(
                        "application/json"
                );

                response.getWriter().write(
                        """
                        {
                            "status": 401,
                            "error": "Unauthorized",
                            "message": "Invalid or inactive API key"
                        }
                        """
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}