package com.sparrowx.internal.features.searchinternalentities;

import java.util.Map;

public record InternalEntitySearchResult(
        String entityId,
        String nodeType,
        String label,
        String slug,
        String summary,
        double score,
        String matchReason,
        String parentEntityId,
        String parentNodeType,
        Map<String, String> attributes
) {
}