package com.sparrowx.document.grpc;

import buildingblocks.infrastructure.grpc.interceptors.*;
import com.sparrowx.document.grpc.interceptors.GrpcPolicyEnforcementInterceptor;
import com.sparrowx.document.grpc.policies.DocumentResiliencePolicy;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    private final GrpcExceptionInterceptor grpcExceptionInterceptor;
    //private final GrpcAuthInterceptor grpcAuthInterceptor;
    //private final GrpcPolicyEnforcementInterceptor grpcPolicyEnforcementInterceptor;
    private final GrpcTracingInterceptor grpcTracingInterceptor;
    private final GrpcMetricsInterceptor grpcMetricsInterceptor;
    private final GrpcLoggingInterceptor grpcLoggingInterceptor;
    private final GrpcRequestContextMdcInterceptor grpcRequestContextMdcInterceptor;

    public GrpcServerConfig(
            GrpcExceptionInterceptor grpcExceptionInterceptor,
            //GrpcAuthInterceptor grpcAuthInterceptor,
            //GrpcPolicyEnforcementInterceptor grpcPolicyEnforcementInterceptor,
            GrpcTracingInterceptor grpcTracingInterceptor,
            GrpcMetricsInterceptor grpcMetricsInterceptor,
            GrpcLoggingInterceptor grpcLoggingInterceptor,
            GrpcRequestContextMdcInterceptor grpcRequestContextMdcInterceptor
    ) {
        this.grpcExceptionInterceptor = grpcExceptionInterceptor;
        //this.grpcAuthInterceptor = grpcAuthInterceptor;
        //this.grpcPolicyEnforcementInterceptor = grpcPolicyEnforcementInterceptor;
        this.grpcTracingInterceptor = grpcTracingInterceptor;
        this.grpcMetricsInterceptor = grpcMetricsInterceptor;
        this.grpcLoggingInterceptor = grpcLoggingInterceptor;
        this.grpcRequestContextMdcInterceptor = grpcRequestContextMdcInterceptor;
    }

    @Bean
    public GrpcResilienceInterceptor.ResiliencePolicyResolver documentResiliencePolicyResolver(
            DocumentResiliencePolicy policy
    ) {
        return fullMethodName -> policy;
    }

    @Bean
    public GrpcResilienceInterceptor grpcResilienceInterceptor(
            GrpcResilienceInterceptor.ResiliencePolicyResolver resolver
    ) {
        return new GrpcResilienceInterceptor(resolver);
    }

    @Bean
    public GrpcServerConfigurer grpcServerConfigurer(
            GrpcResilienceInterceptor grpcResilienceInterceptor
    ) {
        return serverBuilder -> {
            serverBuilder.intercept(grpcExceptionInterceptor);
            //serverBuilder.intercept(grpcAuthInterceptor);
            //serverBuilder.intercept(grpcPolicyEnforcementInterceptor);

            serverBuilder.intercept(grpcTracingInterceptor);
            serverBuilder.intercept(grpcRequestContextMdcInterceptor);

            serverBuilder.intercept(grpcMetricsInterceptor);
            serverBuilder.intercept(grpcLoggingInterceptor);

            serverBuilder.intercept(grpcResilienceInterceptor);
        };
    }
}