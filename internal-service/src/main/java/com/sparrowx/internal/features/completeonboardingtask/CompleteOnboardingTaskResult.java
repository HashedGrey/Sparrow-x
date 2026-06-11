package com.sparrowx.internal.features.completeonboardingtask;

import com.sparrowx.internal.models.EngineerOnboardingTaskProgress;

public record CompleteOnboardingTaskResult(
        EngineerOnboardingTaskProgress taskProgress
) {
}