package com.sparrowx.document.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ElasticsearchConfig.ElasticsearchProperties.class)
public class ElasticsearchConfig {

    @Setter
    @ConfigurationProperties(prefix = "sparrowx.document.elasticsearch")
    public static class ElasticsearchProperties {

        private String indexName = "sparrowx-document-chunks";
        private String url = "http://localhost:9200";
        private boolean enabled = true;

        public String indexName() {
            return indexName;
        }

        public String url() {
            return url;
        }

        public boolean enabled() {
            return enabled;
        }
    }
}