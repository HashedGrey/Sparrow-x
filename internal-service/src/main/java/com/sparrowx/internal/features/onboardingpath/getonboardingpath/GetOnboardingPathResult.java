package com.sparrowx.internal.features.onboardingpath.getonboardingpath;

import com.sparrowx.internal.models.OnboardingPath;
import com.sparrowx.internal.models.OnboardingTask;

import java.util.List;

public record GetOnboardingPathResult(
        OnboardingPath onboardingPath,
        List<OnboardingTask> tasks
) {
    public GetOnboardingPathResult {
        tasks = tasks == null ? List.of() : List.copyOf(tasks);
    }
}