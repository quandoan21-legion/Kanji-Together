package com.example.kanjitogether.dto;

public class GenerateTextRequest {

    private String model;
    private String prompt;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String resolveModel(String fallback) {
        return model == null || model.isBlank() ? fallback : model;
    }
}
