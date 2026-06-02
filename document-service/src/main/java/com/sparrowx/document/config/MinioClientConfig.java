package com.sparrowx.document.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioConfig.class)
@ConditionalOnProperty(
        prefix = "sparrowx.document.minio",
        name = "enabled",
        havingValue = "true"
)
public class MinioClientConfig {

    @Bean
    public MinioClient minioClient(MinioConfig properties) {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }
}