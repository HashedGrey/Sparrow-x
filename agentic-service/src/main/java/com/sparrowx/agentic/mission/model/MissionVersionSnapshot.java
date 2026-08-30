package com.sparrowx.agentic.mission.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Frozen model, prompt, policy and tool versions.
 */
public record MissionVersionSnapshot(
        String snapshotId,
        Map<String, String> modelVersions,
        Map<String, String> promptVersions,
        Map<String, String> policyVersions,
        Map<String, String> toolVersions
) {

    public MissionVersionSnapshot {
        snapshotId = normalize(snapshotId);
        modelVersions = immutableMap(modelVersions);
        promptVersions = immutableMap(promptVersions);
        policyVersions = immutableMap(policyVersions);
        toolVersions = immutableMap(toolVersions);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static Map<String, String> immutableMap(
            Map<String, String> source
    ) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(source)
        );
    }
}