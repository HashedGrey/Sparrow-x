package com.sparrowx.agentic.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.DataConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates Temporal service stubs and the WorkflowClient.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TemporalProperties.class)
public final class TemporalConfig {

    @Bean(destroyMethod = "shutdown")
    public WorkflowServiceStubs workflowServiceStubs(
            TemporalProperties properties
    ) {
        WorkflowServiceStubsOptions options =
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(properties.endpoint())
                        .setRpcTimeout(properties.rpcTimeout())
                        .build();

        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    public WorkflowClient workflowClient(
            WorkflowServiceStubs serviceStubs,
            DataConverter dataConverter,
            TemporalProperties properties
    ) {
        WorkflowClientOptions options =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(properties.namespace())
                        .setIdentity(properties.identity())
                        .setDataConverter(dataConverter)
                        .build();

        return WorkflowClient.newInstance(
                serviceStubs,
                options
        );
    }
}