package com.sparrowx.agentic.mission.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public projection of one currently executing Agentic component.
 */
public record ComponentInvocation(
        String componentId,
        ComponentKind componentKind,
        String componentName,
        Map<String, Object> inputSummary,
        Map<String, Object> outputSummary,
        Instant startedAt,
        Instant completedAt
) {

    public ComponentInvocation {
        componentId = nullToEmpty(componentId);
        componentKind = componentKind == null ? ComponentKind.UNSPECIFIED : componentKind;
        componentName = nullToEmpty(componentName);
        inputSummary = immutableStruct(inputSummary);
        outputSummary = immutableStruct(outputSummary);
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