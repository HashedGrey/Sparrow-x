package com.sparrowx.internal.features.searchinternalentities;

import java.util.List;

public record SearchInternalEntitiesResult(
        List<InternalEntitySearchResult> results,
        boolean ambiguous,
        List<String> warnings
) {
}