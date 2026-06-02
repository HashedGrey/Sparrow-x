package com.sparrowx.document.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@ConfigurationProperties(prefix = "sparrowx.document.minio")
public class MinioConfig {

    private String bucketName = "sparrowx-documents";
    private String endpoint = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private boolean enabled = false;

    public String bucketName() {
        return bucketName;
    }

    public String endpoint() {
        return endpoint;
    }

    public String accessKey() {
        return accessKey;
    }

    public String secretKey() {
        return secretKey;
    }

    public boolean enabled() {
        return enabled;
    }

}