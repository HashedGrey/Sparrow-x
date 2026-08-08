package com.sparrowx.agentic.mission.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One actionable recommendation linked to findings and evidence.
 */
public record Recommendation(
        String recommendationId,
        String title,
        String recommendation,
        String owner,
        String priority,
        double confidenceScore,
        double priorityScore,
        List<String> linkedFindingIds,
        List<String> evidenceIds,
        Map<String, Object> attributes
) {

    public Recommendation {
        recommendationId = nullToEmpty(recommendationId);
        title = nullToEmpty(title);
        recommendation = nullToEmpty(recommendation);
        owner = nullToEmpty(owner);
        priority = nullToEmpty(priority);
        linkedFindingIds = linkedFindingIds == null
                ? List.of()
                : List.copyOf(linkedFindingIds);
        evidenceIds = evidenceIds == null ? List.of() : List.copyOf(evidenceIds);
        attributes = immutableStruct(attributes);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Map<String, Object> immutableStruct(Map<String, Object> value) {
        return value == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }
}