package com.sparrowx.agentic.mission.artifact;

import java.util.Map;

/**
 * Durable references produced while resolving or uploading an input artifact.
 * Raw binary content is never carried into Temporal workflow input.
 */
public record PreparedArtifact(
        String artifactId,
        ArtifactType type,
        String documentId,
        String ingestionJobId,
        String objectUri,
        String externalUri,
        String textReference,
        String filename,
        String contentType,
        String sha256,
        Map<String, String> metadata
) {

    public PreparedArtifact {
        artifactId = nullToEmpty(artifactId);
        type = type == null ? ArtifactType.UNSPECIFIED : type;
        documentId = nullToEmpty(documentId);
        ingestionJobId = nullToEmpty(ingestionJobId);
        objectUri = nullToEmpty(objectUri);
        externalUri = nullToEmpty(externalUri);
        textReference = nullToEmpty(textReference);
        filename = nullToEmpty(filename);
        contentType = nullToEmpty(contentType);
        sha256 = nullToEmpty(sha256);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}