package com.sparrowx.agentic.tools.document;

import com.google.protobuf.ByteString;
import com.sparrowx.agentic.adapters.document.DocumentClientMapper;
import com.sparrowx.agentic.mission.artifact.PreparedArtifact;
import com.sparrowx.document.proto.UploadDocumentRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class UploadDocumentRequestBuilder {

    private final DocumentClientMapper clientMapper;

    public UploadDocumentRequestBuilder(DocumentClientMapper clientMapper) {
        this.clientMapper = Objects.requireNonNull(
                clientMapper,
                "clientMapper must not be null");
    }

    public UploadDocumentRequest build(
            MissionContext context,
            UploadSpec spec) {

        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(spec, "spec must not be null");

        PreparedArtifact artifact = spec.artifact();

        Map<String, String> metadata =
                new LinkedHashMap<>(artifact.metadata());

        metadata.putAll(spec.metadata());
        metadata.put("artifact_id", artifact.artifactId());

        putIfPresent(metadata, "sha256", artifact.sha256());
        putIfPresent(metadata, "object_uri", artifact.objectUri());
        putIfPresent(metadata, "external_uri", artifact.externalUri());
        putIfPresent(metadata, "text_reference", artifact.textReference());

        return UploadDocumentRequest.newBuilder()
                .setContext(clientMapper.toRequestContext(
                        context,
                        spec.requestId()))
                .setFileName(requireText(
                        artifact.filename(),
                        "artifact.filename"))
                .setMimeType(requireText(
                        artifact.contentType(),
                        "artifact.contentType"))
                .setContent(ByteString.copyFrom(spec.content()))
                .setTitle(spec.title().isBlank()
                        ? artifact.filename()
                        : spec.title())
                .addAllTags(spec.tags())
                .putAllMetadata(metadata)
                .build();
    }

    public record UploadSpec(
            String requestId,
            PreparedArtifact artifact,
            byte[] content,
            String title,
            List<String> tags,
            Map<String, String> metadata) {

        public UploadSpec {
            requestId = requireText(requestId, "requestId");
            artifact = Objects.requireNonNull(
                    artifact,
                    "artifact must not be null");

            if (content == null || content.length == 0) {
                throw new IllegalArgumentException(
                        "content must not be empty");
            }

            content = content.clone();
            title = title == null ? "" : title;
            tags = tags == null ? List.of() : List.copyOf(tags);
            metadata = metadata == null
                    ? Map.of()
                    : Map.copyOf(metadata);
        }

        @Override
        public byte[] content() {
            return content.clone();
        }
    }

    private static void putIfPresent(
            Map<String, String> metadata,
            String key,
            String value) {

        if (value != null && !value.isBlank()) {
            metadata.put(key, value);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }
        return value;
    }
}