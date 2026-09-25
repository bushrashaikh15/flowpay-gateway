package com.flowpay.flowpay.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private long totalPayments;
    private long createdPayments;
    private long authorizedPayments;
    private long capturedPayments;
    private long refundedPayments;
    private long failedPayments;

    private BigDecimal capturedAmount;
    private BigDecimal refundedAmount;

    private String mostUsedCurrency;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(
            long totalPayments,
            long createdPayments,
            long authorizedPayments,
            long capturedPayments,
            long refundedPayments,
            long failedPayments,
            BigDecimal capturedAmount,
            BigDecimal refundedAmount,
            String mostUsedCurrency) {

        this.totalPayments = totalPayments;
        this.createdPayments = createdPayments;
        this.authorizedPayments = authorizedPayments;
        this.capturedPayments = capturedPayments;
        this.refundedPayments = refundedPayments;
        this.failedPayments = failedPayments;
        this.capturedAmount = capturedAmount;
        this.refundedAmount = refundedAmount;
        this.mostUsedCurrency = mostUsedCurrency;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public long getCreatedPayments() {
        return createdPayments;
    }

    public void setCreatedPayments(long createdPayments) {
        this.createdPayments = createdPayments;
    }

    public long getAuthorizedPayments() {
        return authorizedPayments;
    }

    public void setAuthorizedPayments(long authorizedPayments) {
        this.authorizedPayments = authorizedPayments;
    }

    public long getCapturedPayments() {
        return capturedPayments;
    }

    public void setCapturedPayments(long capturedPayments) {
        this.capturedPayments = capturedPayments;
    }

    public long getRefundedPayments() {
        return refundedPayments;
    }

    public void setRefundedPayments(long refundedPayments) {
        this.refundedPayments = refundedPayments;
    }

    public long getFailedPayments() {
        return failedPayments;
    }

    public void setFailedPayments(long failedPayments) {
        this.failedPayments = failedPayments;
    }

    public BigDecimal getCapturedAmount() {
        return capturedAmount;
    }

    public void setCapturedAmount(BigDecimal capturedAmount) {
        this.capturedAmount = capturedAmount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public String getMostUsedCurrency() {
        return mostUsedCurrency;
    }

    public void setMostUsedCurrency(String mostUsedCurrency) {
        this.mostUsedCurrency = mostUsedCurrency;
    }
}