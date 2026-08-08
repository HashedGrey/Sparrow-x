package com.sparrowx.agentic.mission.artifact;

import java.util.Map;

/**
 * Normalized submitted artifact preserving the proto oneof content mode.
 */
public record InputArtifact(
        String artifactId,
        ArtifactType type,
        ContentMode contentMode,
        String objectUri,
        byte[] inlineBytes,
        String externalUri,
        String inlineText,
        String filename,
        String contentType,
        String sha256,
        Map<String, String> metadata
) {

    public InputArtifact {
        artifactId = nullToEmpty(artifactId);
        type = type == null ? ArtifactType.UNSPECIFIED : type;
        contentMode = contentMode == null ? ContentMode.UNSPECIFIED : contentMode;
        objectUri = nullToEmpty(objectUri);
        inlineBytes = inlineBytes == null ? new byte[0] : inlineBytes.clone();
        externalUri = nullToEmpty(externalUri);
        inlineText = nullToEmpty(inlineText);
        filename = nullToEmpty(filename);
        contentType = nullToEmpty(contentType);
        sha256 = nullToEmpty(sha256);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public byte[] inlineBytes() {
        return inlineBytes.clone();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public enum ContentMode {
        UNSPECIFIED,
        OBJECT_URI,
        INLINE_BYTES,
        EXTERNAL_URI,
        INLINE_TEXT
    }
}