package com.sparrowx.agentic.config;

import buildingblocks.infrastructure.grpc.interceptors.GrpcAuthInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcExceptionInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcLoggingInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcMetricsInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcTracingInterceptor;
import com.sparrowx.agentic.grpc.interceptors.GrpcTenantContextInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;

/**
 * Registers shared BuildingBlocks gRPC infrastructure and the
 * Agentic-specific tenant boundary.
 */
@Configuration(proxyBeanMethods = false)
public final class GrpcServerConfig {

    @Bean
    @Order(100)
    @GlobalServerInterceptor
    public ServerInterceptor tracingGlobalInterceptor(
            GrpcTracingInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(200)
    @GlobalServerInterceptor
    public ServerInterceptor exceptionGlobalInterceptor(
            GrpcExceptionInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(300)
    @GlobalServerInterceptor
    public ServerInterceptor loggingGlobalInterceptor(
            GrpcLoggingInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(400)
    @GlobalServerInterceptor
    public ServerInterceptor metricsGlobalInterceptor(
            GrpcMetricsInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(500)
    @GlobalServerInterceptor
    public ServerInterceptor authenticationGlobalInterceptor(
            GrpcAuthInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(600)
    @GlobalServerInterceptor
    public ServerInterceptor tenantContextGlobalInterceptor(
            GrpcTenantContextInterceptor interceptor
    ) {
        return interceptor;
    }
}