package com.sparrowx.apigateway.grpc;

import com.sparrowx.apigateway.grpc.clients.SearchGrpcClient;
import com.sparrowx.apigateway.grpc.interceptors.GrpcClientAuthInterceptor;
import com.sparrowx.apigateway.grpc.interceptors.GrpcClientLoggingInterceptor;
import com.sparrowx.apigateway.grpc.interceptors.GrpcClientMetricsInterceptor;
import com.sparrowx.apigateway.grpc.interceptors.GrpcClientResilienceInterceptor;
import com.sparrowx.apigateway.grpc.interceptors.GrpcClientTracingInterceptor;
import com.sparrowx.apigateway.grpc.policies.search.SearchResiliencePolicy;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    private ManagedChannel baseChannel(
            String target,
            GrpcClientTracingInterceptor tracingInterceptor,
            GrpcClientMetricsInterceptor metricsInterceptor,
            GrpcClientLoggingInterceptor loggingInterceptor,
            GrpcClientAuthInterceptor authInterceptor,
            //GrpcClientDebugInterceptor debugInterceptor,
            GrpcClientResilienceInterceptor resilienceInterceptor
    ) {
        return ManagedChannelBuilder.forTarget(target)
                .usePlaintext() // Linkerd handles mTLS
                .intercept(
                        tracingInterceptor,
                        metricsInterceptor,
                        loggingInterceptor,
                        authInterceptor,
                        resilienceInterceptor
                        //debugInterceptor
                )
                .build();
    }

    /* =========================
       Agentic
       ========================= */
//    @Bean
//    public AgenticGrpcClient agenticGrpcClient(...) { ... }

    /* =========================
       Profile
       ========================= */
//    @Bean
//    public ProfileGrpcClient profileGrpcClient(...) { ... }

    /* =========================
       Tweet
       ========================= */
//    @Bean
//    public TweetGrpcClient tweetGrpcClient(...) { ... }

    /* =========================
       Timeline
       ========================= */
//    @Bean
//    public TimelineGrpcClient timelineGrpcClient(...) { ... }

    /* =========================
       Search
       ========================= */
    @Bean
    public SearchGrpcClient searchGrpcClient(
            GrpcClientTracingInterceptor tracingInterceptor,
            GrpcClientMetricsInterceptor metricsInterceptor,
            GrpcClientLoggingInterceptor loggingInterceptor,
            GrpcClientAuthInterceptor authInterceptor,
            //GrpcClientDebugInterceptor debugInterceptor,
            SearchResiliencePolicy resiliencePolicy
    ) {
        ManagedChannel channel = baseChannel(
                "search-service",
                tracingInterceptor,
                metricsInterceptor,
                loggingInterceptor,
                authInterceptor,
                //debugInterceptor,
                new GrpcClientResilienceInterceptor(resiliencePolicy)
        );

        return new SearchGrpcClient(channel);
    }
}