package com.sparrowx.apigateway.grpc;

import buildingblocks.infrastructure.grpc.interceptors.GrpcDebugInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcObservabilityInterceptor;
import buildingblocks.infrastructure.grpc.interceptors.GrpcResilienceInterceptor;
import com.sparrowx.apigateway.grpc.policies.agentic.AgenticResiliencePolicy;
import com.sparrowx.apigateway.grpc.policies.profile.ProfileResiliencePolicy;
import com.sparrowx.apigateway.grpc.policies.search.SearchResiliencePolicy;
import com.sparrowx.apigateway.grpc.policies.timeline.TimelineResiliencePolicy;
import com.sparrowx.apigateway.grpc.policies.tweet.TweetResiliencePolicy;
import com.distributedx.apigateway.grpc.stubs.*;
import com.sparrowx.apigateway.grpc.stubs.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    private ManagedChannel baseChannel(String target,
                                       GrpcObservabilityInterceptor observabilityInterceptor,
                                       GrpcDebugInterceptor debugInterceptor,
                                       GrpcResilienceInterceptor resilienceInterceptor) {

        return ManagedChannelBuilder.forTarget(target)
                .usePlaintext() // Linkerd handles mTLS
                .intercept(
                        observabilityInterceptor,
                        debugInterceptor,
                        resilienceInterceptor
                )
                .build();
    }

    /* =========================
       Agentic
       ========================= */
    @Bean
    public AgenticGrpcClient agenticGrpcClient(
            GrpcObservabilityInterceptor observabilityInterceptor,
            GrpcDebugInterceptor debugInterceptor,
            AgenticResiliencePolicy resiliencePolicy) {

        ManagedChannel channel = baseChannel(
                "agentic-service",
                observabilityInterceptor,
                debugInterceptor,
                new GrpcResilienceInterceptor(resiliencePolicy)
        );

        return new AgenticGrpcClient(channel);
    }

    /* =========================
       Profile
       ========================= */
    @Bean
    public ProfileGrpcClient profileGrpcClient(
            GrpcObservabilityInterceptor observabilityInterceptor,
            GrpcDebugInterceptor debugInterceptor,
            ProfileResiliencePolicy resiliencePolicy) {

        ManagedChannel channel = baseChannel(
                "profile-service",
                observabilityInterceptor,
                debugInterceptor,
                new GrpcResilienceInterceptor(resiliencePolicy)
        );

        return new ProfileGrpcClient(channel);
    }

    /* =========================
       Tweet
       ========================= */
    @Bean
    public TweetGrpcClient tweetGrpcClient(
            GrpcObservabilityInterceptor observabilityInterceptor,
            GrpcDebugInterceptor debugInterceptor,
            TweetResiliencePolicy resiliencePolicy) {

        ManagedChannel channel = baseChannel(
                "tweet-service",
                observabilityInterceptor,
                debugInterceptor,
                new GrpcResilienceInterceptor(resiliencePolicy)
        );

        return new TweetGrpcClient(channel);
    }

    /* =========================
       Timeline
       ========================= */
    @Bean
    public TimelineGrpcClient timelineGrpcClient(
            GrpcObservabilityInterceptor observabilityInterceptor,
            GrpcDebugInterceptor debugInterceptor,
            TimelineResiliencePolicy resiliencePolicy) {

        ManagedChannel channel = baseChannel(
                "timeline-service",
                observabilityInterceptor,
                debugInterceptor,
                new GrpcResilienceInterceptor(resiliencePolicy)
        );

        return new TimelineGrpcClient(channel);
    }

    /* =========================
       Search
       ========================= */
    @Bean
    public SearchGrpcClient searchGrpcClient(
            GrpcObservabilityInterceptor observabilityInterceptor,
            GrpcDebugInterceptor debugInterceptor,
            SearchResiliencePolicy resiliencePolicy) {

        ManagedChannel channel = baseChannel(
                "search-service",
                observabilityInterceptor,
                debugInterceptor,
                new GrpcResilienceInterceptor(resiliencePolicy)
        );

        return new SearchGrpcClient(channel);
    }
}
