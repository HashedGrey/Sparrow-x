package com.sparrowx.agentic.mission.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One structured, evidence-linked finding in the final result.
 */
public record Finding(
        String findingId,
        String title,
        String summary,
        FindingType type,
        double confidenceScore,
        double severityScore,
        double priorityScore,
        List<String> relatedFindingIds,
        List<String> evidenceIds,
        Map<String, Object> attributes
) {

    public Finding {
        findingId = nullToEmpty(findingId);
        title = nullToEmpty(title);
        summary = nullToEmpty(summary);
        type = type == null ? FindingType.UNSPECIFIED : type;
        relatedFindingIds = relatedFindingIds == null
                ? List.of()
                : List.copyOf(relatedFindingIds);
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