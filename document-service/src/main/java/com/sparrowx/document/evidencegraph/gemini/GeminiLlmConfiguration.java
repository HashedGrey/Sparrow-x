package com.sparrowx.document.evidencegraph.gemini;

import com.google.genai.Client;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeminiLlmProperties.class)
public class GeminiLlmConfiguration {

    @Bean
    public Client geminiClient(GeminiLlmProperties properties) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            return new Client();
        }

        return Client.builder()
                .apiKey(properties.apiKey())
                .build();
    }
}