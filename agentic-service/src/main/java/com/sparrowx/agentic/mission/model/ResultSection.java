package com.sparrowx.agentic.mission.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One ordered section of the final mission answer.
 */
public record ResultSection(
        String sectionId,
        String title,
        String body,
        int order,
        List<String> findingIds,
        List<String> recommendationIds,
        List<String> evidenceIds,
        Map<String, Object> attributes
) {

    public ResultSection {
        sectionId = nullToEmpty(sectionId);
        title = nullToEmpty(title);
        body = nullToEmpty(body);
        findingIds = findingIds == null ? List.of() : List.copyOf(findingIds);
        recommendationIds = recommendationIds == null
                ? List.of()
                : List.copyOf(recommendationIds);
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