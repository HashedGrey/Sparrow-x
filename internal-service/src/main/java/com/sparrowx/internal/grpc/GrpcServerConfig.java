package com.sparrowx.internal.grpc;

import buildingblocks.infrastructure.grpc.interceptors.GrpcLoggingInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcMetricsInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcResilienceInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcTracingInterceptor;

import com.sparrowx.internal.grpc.policies.InternalResiliencePolicy;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    // private final GrpcAuthInterceptor grpcAuthInterceptor;

    //private final RedisCacheProvider redisCacheProvider;
    private final GrpcLoggingInterceptor grpcLoggingInterceptor;
    private final GrpcMetricsInterceptor grpcMetricsInterceptor;
    private final GrpcTracingInterceptor grpcTracingInterceptor;
    //private final GrpcDebugInterceptor grpcDebugInterceptor;

    public GrpcServerConfig(
            //GrpcAuthInterceptor grpcAuthInterceptor,
            //RedisCacheProvider redisCacheProvider,
            GrpcLoggingInterceptor grpcLoggingInterceptor,
            GrpcMetricsInterceptor grpcMetricsInterceptor,
            GrpcTracingInterceptor grpcTracingInterceptor
            //GrpcDebugInterceptor grpcDebugInterceptor
    ) {
        // this.grpcAuthInterceptor = grpcAuthInterceptor;
        //this.redisCacheProvider = redisCacheProvider;
        this.grpcLoggingInterceptor = grpcLoggingInterceptor;
        this.grpcMetricsInterceptor = grpcMetricsInterceptor;
        this.grpcTracingInterceptor = grpcTracingInterceptor;
        //this.grpcDebugInterceptor = grpcDebugInterceptor;
    }

//    @Bean
//    public GrpcCachingInterceptor grpcCachingInterceptor() {
//        InternalCachePolicy policy = new InternalCachePolicy(
//                redisCacheProvider,
//                "internal:"
//        );
//
//        return new GrpcCachingInterceptor(policy);
//    }

    @Bean
    public GrpcResilienceInterceptor.ResiliencePolicyResolver internalResiliencePolicyResolver(
            InternalResiliencePolicy policy
    ) {
        return fullMethodName -> policy;
    }

//    @Bean
//    public GrpcResilienceInterceptor grpcResilienceInterceptor(
//            GrpcResilienceInterceptor.ResiliencePolicyResolver resolver
//    ) {
//        return new GrpcResilienceInterceptor(resolver);
//    }

    @Bean
    public GrpcServerConfigurer grpcServerConfigurer(
            //GrpcCachingInterceptor grpcCachingInterceptor,
            GrpcResilienceInterceptor grpcResilienceInterceptor
    ) {
        return serverBuilder -> {

            // 1. SECURITY
            // serverBuilder.intercept(grpcAuthInterceptor);

            // 2. OBSERVABILITY
            serverBuilder.intercept(grpcTracingInterceptor);
            serverBuilder.intercept(grpcMetricsInterceptor);
            serverBuilder.intercept(grpcLoggingInterceptor);

//            // 3. FEATURE TOGGLES
//            serverBuilder.intercept(grpcDebugInterceptor);
//
//            // 4. CACHE
//            serverBuilder.intercept(grpcCachingInterceptor);
//
//            // 5. RESILIENCE
            serverBuilder.intercept(grpcResilienceInterceptor);
        };
    }
}