package com.sparrowx.agentic.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Agentic-owned runtime dependencies.
 *
 * OpenTelemetry configuration, tracing, logging, and metrics are supplied
 * by BuildingBlocks.
 */
@Configuration(proxyBeanMethods = false)
public final class ObservabilityConfig {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock agenticClock() {
        return Clock.systemUTC();
    }
}