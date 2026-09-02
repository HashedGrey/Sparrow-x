package com.sparrowx.document.grpc;

import buildingblocks.infrastructure.grpc.interceptors.GrpcAuthInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcExceptionInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcLoggingInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcMetricsInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcRequestContextMdcInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcResilienceInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcTracingInterceptor;
import com.sparrowx.document.grpc.policies.DocumentResiliencePolicy;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;

@Configuration(proxyBeanMethods = false)
public final class GrpcServerConfig {

    @Bean
    @Order(100)
    @GlobalServerInterceptor
    public GrpcTracingInterceptor grpcTracingInterceptor(
            Tracer tracer
    ) {
        return new GrpcTracingInterceptor(tracer);
    }

    @Bean
    @Order(200)
    @GlobalServerInterceptor
    public GrpcExceptionInterceptor grpcExceptionInterceptor() {
        return new GrpcExceptionInterceptor();
    }

    @Bean
    @Order(300)
    @GlobalServerInterceptor
    public GrpcRequestContextMdcInterceptor grpcRequestContextMdcInterceptor() {
        return new GrpcRequestContextMdcInterceptor();
    }

    @Bean
    @Order(400)
    @GlobalServerInterceptor
    public GrpcLoggingInterceptor grpcLoggingInterceptor() {
        return new GrpcLoggingInterceptor();
    }

    @Bean
    @Order(500)
    @GlobalServerInterceptor
    public GrpcMetricsInterceptor grpcMetricsInterceptor() {
        return new GrpcMetricsInterceptor();
    }

    @Bean
    @Order(600)
    @GlobalServerInterceptor
    public GrpcAuthInterceptor grpcAuthInterceptor() {
        return new GrpcAuthInterceptor();
    }

//    @Bean
//    @Order(700)
//    @GlobalServerInterceptor
//    public GrpcPolicyEnforcementInterceptor grpcPolicyEnforcementInterceptor() {
//        return new GrpcPolicyEnforcementInterceptor();
//    }

    @Bean
    public GrpcResilienceInterceptor.ResiliencePolicyResolver documentResiliencePolicyResolver(DocumentResiliencePolicy policy) {
        return fullMethodName -> policy;
    }

    @Bean
    @Order(800)
    @GlobalServerInterceptor
    public GrpcResilienceInterceptor grpcResilienceInterceptor(GrpcResilienceInterceptor.ResiliencePolicyResolver resolver) {
        return new GrpcResilienceInterceptor(resolver);
    }
}