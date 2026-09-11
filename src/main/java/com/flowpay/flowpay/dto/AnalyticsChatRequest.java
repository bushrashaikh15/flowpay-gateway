package com.flowpay.flowpay.dto;

public class AnalyticsChatRequest {

    private String question;

    public AnalyticsChatRequest() {
    }

    public AnalyticsChatRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}