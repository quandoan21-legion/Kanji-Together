package com.example.kanjitogether.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;

@Configuration
public class GenAiConfig {

    @Bean
    public Client genAiClient(@Value("${gemini.api.key:${GEMINI_API_KEY:}}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set GEMINI_API_KEY env var or gemini.api.key property.");
        }
        return Client.builder().apiKey(apiKey).build();
    }
}
