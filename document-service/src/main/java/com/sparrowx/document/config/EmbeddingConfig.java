package com.sparrowx.document.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmbeddingConfig.EmbeddingProperties.class)
public class EmbeddingConfig {

    @Setter
    @ConfigurationProperties(prefix = "sparrowx.document.embedding")
    public static class EmbeddingProperties {

        private String provider = "deterministic";
        private String model = "gemini-embedding-001";
        private int dimension = 768;
        private String apiKey = "";

        public String provider() {
            return provider;
        }

        public String model() {
            return model;
        }

        public int dimension() {
            return dimension;
        }

        public String apiKey() {
            return apiKey;
        }
    }
}