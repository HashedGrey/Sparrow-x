package com.sparrowx.document.evidencegraph.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sparrowx.document.llm")
public record GeminiLlmProperties(
        String provider,
        String model,
        String apiKey,
        int maxOutputTokens,
        double temperature,
        double topP,
        int projectionRetryCount,
        int maxProjectionSpans,
        int maxExcerptChars,
        boolean jsonRepairEnabled
) {
    public GeminiLlmProperties {
        provider = provider == null || provider.isBlank() ? "gemini" : provider;
        model = model == null || model.isBlank() ? "gemini-2.5-flash" : model;
        apiKey = apiKey == null ? "" : apiKey;

        maxOutputTokens = maxOutputTokens <= 0 ? 8192 : maxOutputTokens;

        temperature = temperature < 0.0 ? 0.0 : Math.min(1.0, temperature);
        topP = topP <= 0.0 ? 0.95 : Math.min(1.0, topP);

        projectionRetryCount = projectionRetryCount < 0 ? 1 : projectionRetryCount;
        maxProjectionSpans = maxProjectionSpans <= 0 ? 6 : maxProjectionSpans;
        maxExcerptChars = maxExcerptChars <= 0 ? 1_200 : maxExcerptChars;

        jsonRepairEnabled = jsonRepairEnabled;
    }
}