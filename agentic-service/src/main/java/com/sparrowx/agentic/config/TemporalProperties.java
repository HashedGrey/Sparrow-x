package com.sparrowx.agentic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Temporal endpoint, namespace, task queue and worker execution settings.
 */
@ConfigurationProperties(prefix = "sparrowx.agentic.temporal")
public record TemporalProperties(
        @DefaultValue("127.0.0.1:7233")
        String endpoint,

        @DefaultValue("default")
        String namespace,

        @DefaultValue("sparrowx-agentic")
        String taskQueue,

        @DefaultValue("sparrowx-agentic-service")
        String identity,

        @DefaultValue("true")
        boolean workerEnabled,

        @DefaultValue("PT10S")
        Duration rpcTimeout,

        @DefaultValue("100")
        int maxConcurrentWorkflowTaskExecutions,

        @DefaultValue("100")
        int maxConcurrentActivityExecutions,

        @DefaultValue("2")
        int maxConcurrentWorkflowTaskPollers,

        @DefaultValue("4")
        int maxConcurrentActivityTaskPollers
) {

    public TemporalProperties {
        endpoint = requireText(endpoint, "endpoint");
        namespace = requireText(namespace, "namespace");
        taskQueue = requireText(taskQueue, "taskQueue");
        identity = requireText(identity, "identity");

        if (rpcTimeout == null
                || rpcTimeout.isZero()
                || rpcTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "rpcTimeout must be positive"
            );
        }

        requirePositive(
                maxConcurrentWorkflowTaskExecutions,
                "maxConcurrentWorkflowTaskExecutions"
        );

        requirePositive(
                maxConcurrentActivityExecutions,
                "maxConcurrentActivityExecutions"
        );

        requirePositive(
                maxConcurrentWorkflowTaskPollers,
                "maxConcurrentWorkflowTaskPollers"
        );

        requirePositive(
                maxConcurrentActivityTaskPollers,
                "maxConcurrentActivityTaskPollers"
        );
    }

    private static void requirePositive(
            int value,
            String field
    ) {
        if (value < 1) {
            throw new IllegalArgumentException(
                    field + " must be positive"
            );
        }
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }
}