package com.sparrowx.tweet;

import io.github.resilience4j.retry.*;
import org.springframework.context.annotation.*;

import java.time.Duration;

@Configuration
public class TweetServiceConfigurations {

    @Bean
    public Retry eventRetry() {

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(200))
                .build();

        return Retry.of("eventbus", config);
    }
}