package com.sparrowx.agentic.mission.evidence;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalized source provenance included in a mission result.
 */
public record EvidenceRef(
        String evidenceId,
        EvidenceSourceType sourceType,
        String sourceService,
        String sourceId,
        String sourceUri,
        String artifactId,
        String objectId,
        String parentObjectId,
        String locationLabel,
        int pageStart,
        int pageEnd,
        String section,
        String chunkId,
        String sha256,
        Map<String, Object> attributes
) {

    public EvidenceRef {
        evidenceId = nullToEmpty(evidenceId);
        sourceType = sourceType == null ? EvidenceSourceType.UNSPECIFIED : sourceType;
        sourceService = nullToEmpty(sourceService);
        sourceId = nullToEmpty(sourceId);
        sourceUri = nullToEmpty(sourceUri);
        artifactId = nullToEmpty(artifactId);
        objectId = nullToEmpty(objectId);
        parentObjectId = nullToEmpty(parentObjectId);
        locationLabel = nullToEmpty(locationLabel);
        section = nullToEmpty(section);
        chunkId = nullToEmpty(chunkId);
        sha256 = nullToEmpty(sha256);
        attributes = immutableStruct(attributes);
    }

    public EvidenceRef withEvidenceId(String assignedEvidenceId) {
        return new EvidenceRef(
                assignedEvidenceId,
                sourceType,
                sourceService,
                sourceId,
                sourceUri,
                artifactId,
                objectId,
                parentObjectId,
                locationLabel,
                pageStart,
                pageEnd,
                section,
                chunkId,
                sha256,
                attributes
        );
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