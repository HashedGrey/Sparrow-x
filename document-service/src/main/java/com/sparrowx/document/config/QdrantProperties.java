package com.sparrowx.document.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@ConfigurationProperties(prefix = "sparrowx.document.qdrant")
public class QdrantProperties {

    private String collectionName = "sparrowx_document_chunks";
    private String url = "http://localhost:6333";
    private int vectorDimension = 384;
    private boolean enabled = false;

    public String collectionName() {
        return collectionName;
    }

    public String url() {
        return url;
    }

    public int vectorDimension() {
        return vectorDimension;
    }

    public boolean enabled() {
        return enabled;
    }

}