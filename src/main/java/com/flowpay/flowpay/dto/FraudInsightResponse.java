package com.flowpay.flowpay.dto;

import java.util.List;

public class FraudInsightResponse {

    private Long paymentIntentId;

    private int riskScore;

    private String riskLevel;

    private List<String> riskSignals;

    private String aiExplanation;

    private String disclaimer;

    public FraudInsightResponse() {
    }

    public FraudInsightResponse(
            Long paymentIntentId,
            int riskScore,
            String riskLevel,
            List<String> riskSignals,
            String aiExplanation,
            String disclaimer) {

        this.paymentIntentId = paymentIntentId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.riskSignals = riskSignals;
        this.aiExplanation = aiExplanation;
        this.disclaimer = disclaimer;
    }

    public Long getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(Long paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getRiskSignals() {
        return riskSignals;
    }

    public void setRiskSignals(List<String> riskSignals) {
        this.riskSignals = riskSignals;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}