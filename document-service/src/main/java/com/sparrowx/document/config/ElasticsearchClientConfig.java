package com.sparrowx.document.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "sparrowx.document.elasticsearch",
        name = "enabled",
        havingValue = "true"
)
public class ElasticsearchClientConfig {

    private final ElasticsearchConfig.ElasticsearchProperties properties;

    public ElasticsearchClientConfig(
            ElasticsearchConfig.ElasticsearchProperties properties
    ) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "close")
    public RestClient elasticsearchRestClient() {
        return RestClient.builder(HttpHost.create(properties.url()))
                .build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}