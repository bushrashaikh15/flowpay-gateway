package com.flowpay.flowpay.dto;

public class MonthlyPaymentReportResponse {

    private int year;

    private int month;

    private long totalPayments;

    private long createdPayments;

    private long authorizedPayments;

    private long capturedPayments;

    private long refundedPayments;

    private long failedPayments;

    private double totalPaymentAmount;

    private double totalCapturedAmount;

    private double totalRefundedAmount;

    private String mostUsedCurrency;

    private String aiSummary;


    public MonthlyPaymentReportResponse() {
    }


    public MonthlyPaymentReportResponse(
            int year,
            int month,
            long totalPayments,
            long createdPayments,
            long authorizedPayments,
            long capturedPayments,
            long refundedPayments,
            long failedPayments,
            double totalPaymentAmount,
            double totalCapturedAmount,
            double totalRefundedAmount,
            String mostUsedCurrency,
            String aiSummary) {

        this.year = year;

        this.month = month;

        this.totalPayments = totalPayments;

        this.createdPayments = createdPayments;

        this.authorizedPayments = authorizedPayments;

        this.capturedPayments = capturedPayments;

        this.refundedPayments = refundedPayments;

        this.failedPayments = failedPayments;

        this.totalPaymentAmount = totalPaymentAmount;

        this.totalCapturedAmount = totalCapturedAmount;

        this.totalRefundedAmount = totalRefundedAmount;

        this.mostUsedCurrency = mostUsedCurrency;

        this.aiSummary = aiSummary;
    }


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }


    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
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

    public void setAuthorizedPayments(
            long authorizedPayments) {

        this.authorizedPayments =
                authorizedPayments;
    }


    public long getCapturedPayments() {
        return capturedPayments;
    }

    public void setCapturedPayments(
            long capturedPayments) {

        this.capturedPayments =
                capturedPayments;
    }


    public long getRefundedPayments() {
        return refundedPayments;
    }

    public void setRefundedPayments(
            long refundedPayments) {

        this.refundedPayments =
                refundedPayments;
    }


    public long getFailedPayments() {
        return failedPayments;
    }

    public void setFailedPayments(
            long failedPayments) {

        this.failedPayments =
                failedPayments;
    }


    public double getTotalPaymentAmount() {
        return totalPaymentAmount;
    }

    public void setTotalPaymentAmount(
            double totalPaymentAmount) {

        this.totalPaymentAmount =
                totalPaymentAmount;
    }


    public double getTotalCapturedAmount() {
        return totalCapturedAmount;
    }

    public void setTotalCapturedAmount(
            double totalCapturedAmount) {

        this.totalCapturedAmount =
                totalCapturedAmount;
    }


    public double getTotalRefundedAmount() {
        return totalRefundedAmount;
    }

    public void setTotalRefundedAmount(
            double totalRefundedAmount) {

        this.totalRefundedAmount =
                totalRefundedAmount;
    }


    public String getMostUsedCurrency() {
        return mostUsedCurrency;
    }

    public void setMostUsedCurrency(
            String mostUsedCurrency) {

        this.mostUsedCurrency =
                mostUsedCurrency;
    }


    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(
            String aiSummary) {

        this.aiSummary =
                aiSummary;
    }
}