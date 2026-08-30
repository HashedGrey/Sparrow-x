package com.sparrowx.agentic.mission.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Source, tool, citation, review and runtime constraints.
 */
public record MissionConstraints(
        MissionPath preferredPath,
        List<String> allowedTools,
        List<String> allowedSourceServices,
        List<String> requiredOutputSections,
        boolean requireCitations,
        boolean requireHumanReview,
        boolean allowExternalSources,
        Duration maxRuntime,
        Map<String, String> policyHints
) {

    public MissionConstraints {
        allowedTools = immutableList(allowedTools);
        allowedSourceServices = immutableList(allowedSourceServices);
        requiredOutputSections = immutableList(requiredOutputSections);
        policyHints = immutableMap(policyHints);
    }

    private static <T> List<T> immutableList(List<T> source) {
        if (source == null) {
            return null;
        }

        return Collections.unmodifiableList(
                new ArrayList<>(source)
        );
    }

    private static <K, V> Map<K, V> immutableMap(
            Map<K, V> source
    ) {
        if (source == null) {
            return null;
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(source)
        );
    }
}