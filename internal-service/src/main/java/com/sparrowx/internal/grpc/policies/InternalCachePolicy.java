package com.sparrowx.internal.grpc.policies;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class InternalCachePolicy {

    public boolean cacheable(String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return false;
        }

        return methodName.contains("/Get")
                || methodName.contains("/Read");
    }

    public boolean cacheMutation(String methodName) {
        return false;
    }

    public Duration ttl(String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return Duration.ZERO;
        }

        if (methodName.contains("/ReadInternalCompanyGraph")
                || methodName.contains("/ReadLearningGraph")) {
            return Duration.ofMinutes(10);
        }

        if (methodName.contains("/GetEngineerOnboardingProgress")) {
            return Duration.ofSeconds(30);
        }

        if (methodName.contains("/Get")) {
            return Duration.ofMinutes(2);
        }

        return Duration.ZERO;
    }

    public boolean shouldEvictOnMutation(String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return false;
        }

        return methodName.contains("/Create")
                || methodName.contains("/Assign")
                || methodName.contains("/Complete");
    }
}