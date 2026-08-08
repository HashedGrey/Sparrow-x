package com.sparrowx.internal.grpc;

import buildingblocks.infrastructure.grpc.interceptors.GrpcExceptionInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcLoggingInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcMetricsInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcTracingInterceptor;
import com.sparrowx.agentic.grpc.interceptors.GrpcAuthInterceptor;
import com.sparrowx.agentic.grpc.interceptors.GrpcTenantContextInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;

/**
 * Registers shared BuildingBlocks interceptors and Agentic-specific
 * security policies in deterministic order.
 */
@Configuration(proxyBeanMethods = false)
public final class GrpcServerConfig {

    @Bean
    @Order(100)
    @GlobalServerInterceptor
    public ServerInterceptor grpcTracingInterceptor(
            GrpcTracingInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(200)
    @GlobalServerInterceptor
    public ServerInterceptor grpcExceptionInterceptor(
            GrpcExceptionInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(300)
    @GlobalServerInterceptor
    public ServerInterceptor grpcLoggingInterceptor(
            GrpcLoggingInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(400)
    @GlobalServerInterceptor
    public ServerInterceptor grpcMetricsInterceptor(
            GrpcMetricsInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(500)
    @GlobalServerInterceptor
    public ServerInterceptor agenticAuthInterceptor(
            GrpcAuthInterceptor interceptor
    ) {
        return interceptor;
    }

    @Bean
    @Order(600)
    @GlobalServerInterceptor
    public ServerInterceptor agenticTenantContextInterceptor(
            GrpcTenantContextInterceptor interceptor
    ) {
        return interceptor;
    }
}