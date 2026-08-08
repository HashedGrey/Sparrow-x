package com.sparrowx.agentic.validation;

import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.mission.evidence.EvidenceSourceType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public final class DownstreamResponseValidator {

    private static final Pattern SHA_256 =
            Pattern.compile("[0-9a-fA-F]{64}");

    private static final Set<EvidenceSourceType> DOCUMENT_EVIDENCE_TYPES =
            EnumSet.of(
                    EvidenceSourceType.INPUT_ARTIFACT,
                    EvidenceSourceType.DOCUMENT,
                    EvidenceSourceType.DOCUMENT_SPAN,
                    EvidenceSourceType.TOOL_RESULT
            );

    private static final Set<EvidenceSourceType> INTERNAL_EVIDENCE_TYPES =
            EnumSet.of(
                    EvidenceSourceType.INTERNAL_ENTITY,
                    EvidenceSourceType.INTERNAL_GRAPH,
                    EvidenceSourceType.TOOL_RESULT
            );

    private final Limits limits;

    public DownstreamResponseValidator() {
        this(Limits.defaults());
    }

    private DownstreamResponseValidator(Limits limits) {
        this.limits = Objects.requireNonNull(
                limits,
                "limits must not be null"
        );
    }

    public static DownstreamResponseValidator configured(
            Limits limits
    ) {
        return new DownstreamResponseValidator(limits);
    }

    /**
     * Keeps generated Document Service proto types out of this validator while
     * preserving the concrete response type for callers.
     */
    public <T> T validateDocument(
            String operation,
            String expectedTenantId,
            T response,
            ResponseInspector<T> inspector
    ) {
        return validate(
                ServiceKind.DOCUMENT,
                operation,
                expectedTenantId,
                response,
                inspector,
                DOCUMENT_EVIDENCE_TYPES
        );
    }

    /**
     * Keeps generated Internal Service proto types out of this validator while
     * preserving the concrete response type for callers.
     */
    public <T> T validateInternal(
            String operation,
            String expectedTenantId,
            T response,
            ResponseInspector<T> inspector
    ) {
        return validate(
                ServiceKind.INTERNAL,
                operation,
                expectedTenantId,
                response,
                inspector,
                INTERNAL_EVIDENCE_TYPES
        );
    }

    private <T> T validate(
            ServiceKind serviceKind,
            String operation,
            String expectedTenantId,
            T response,
            ResponseInspector<T> inspector,
            Set<EvidenceSourceType> allowedEvidenceTypes
    ) {
        requireText(operation, "operation");
        String tenantId = requireText(
                expectedTenantId,
                "expectedTenantId"
        );

        if (response == null) {
            throw violation(
                    serviceKind,
                    "RESPONSE_REQUIRED",
                    "response must not be null"
            );
        }

        if (inspector == null) {
            throw violation(
                    serviceKind,
                    "INSPECTOR_REQUIRED",
                    "response inspector must not be null"
            );
        }

        ResponseMetadata metadata = inspector.inspect(response);
        if (metadata == null) {
            throw violation(
                    serviceKind,
                    "METADATA_REQUIRED",
                    "response inspector must return metadata"
            );
        }

        if (!tenantId.equals(metadata.tenantId())) {
            throw violation(
                    serviceKind,
                    "TENANT_MISMATCH",
                    "response tenant does not match the mission tenant"
            );
        }

        requireText(metadata.requestId(), "response.requestId");
        requireText(metadata.responseId(), "response.responseId");

        if (metadata.itemCount() < 0
                || metadata.itemCount() > limits.maxItems()) {
            throw violation(
                    serviceKind,
                    "ITEM_COUNT_OUT_OF_RANGE",
                    "item count exceeds " + limits.maxItems()
            );
        }

        if (metadata.serializedSizeBytes() < 0L
                || metadata.serializedSizeBytes()
                > limits.maxSerializedResponseBytes()) {
            throw violation(
                    serviceKind,
                    "RESPONSE_SIZE_OUT_OF_RANGE",
                    "serialized response exceeds "
                            + limits.maxSerializedResponseBytes()
            );
        }

        for (EvidenceRef evidence : metadata.evidenceRefs()) {
            validateEvidence(
                    serviceKind,
                    evidence,
                    allowedEvidenceTypes
            );
        }

        return response;
    }

    private static void validateEvidence(
            ServiceKind serviceKind,
            EvidenceRef evidence,
            Set<EvidenceSourceType> allowedTypes
    ) {
        if (evidence == null) {
            throw violation(
                    serviceKind,
                    "NULL_EVIDENCE",
                    "evidenceRefs must not contain null"
            );
        }

        if (evidence.sourceType() == null
                || !allowedTypes.contains(evidence.sourceType())) {
            throw violation(
                    serviceKind,
                    "EVIDENCE_SOURCE_TYPE",
                    "evidence has an unauthorized source type"
            );
        }

        requireText(
                evidence.sourceService(),
                "evidence.sourceService"
        );

        boolean hasStableSourceIdentity = hasText(evidence.sourceId())
                || hasText(evidence.artifactId())
                || hasText(evidence.objectId())
                || hasText(evidence.chunkId());

        if (!hasStableSourceIdentity) {
            throw violation(
                    serviceKind,
                    "EVIDENCE_IDENTITY_REQUIRED",
                    "evidence must contain a stable source identity"
            );
        }

        if (evidence.pageStart() < 0
                || evidence.pageEnd() < 0
                || (evidence.pageEnd() > 0
                && evidence.pageStart() > evidence.pageEnd())) {
            throw violation(
                    serviceKind,
                    "EVIDENCE_PAGE_RANGE",
                    "evidence page range is invalid"
            );
        }

        if (hasText(evidence.sha256())
                && !SHA_256.matcher(evidence.sha256()).matches()) {
            throw violation(
                    serviceKind,
                    "EVIDENCE_SHA256",
                    "evidence sha256 is invalid"
            );
        }
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(
                    "DOWNSTREAM_RESPONSE_FIELD_REQUIRED: "
                            + field + " must not be blank"
            );
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static IllegalArgumentException violation(
            ServiceKind serviceKind,
            String code,
            String detail
    ) {
        return new IllegalArgumentException(
                "DOWNSTREAM_" + serviceKind.name()
                        + "_" + code + ": " + detail
        );
    }

    @FunctionalInterface
    public interface ResponseInspector<T> {
        ResponseMetadata inspect(T response);
    }

    public record ResponseMetadata(
            String tenantId,
            String requestId,
            String responseId,
            int itemCount,
            long serializedSizeBytes,
            List<EvidenceRef> evidenceRefs
    ) {
        public ResponseMetadata {
            tenantId = requireText(
                    tenantId,
                    "response.tenantId"
            );
            requestId = requireText(
                    requestId,
                    "response.requestId"
            );
            responseId = requireText(
                    responseId,
                    "response.responseId"
            );
            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : Collections.unmodifiableList(
                    new ArrayList<>(evidenceRefs)
            );
        }
    }

    public record Limits(
            int maxItems,
            long maxSerializedResponseBytes
    ) {
        public Limits {
            if (maxItems < 1 || maxSerializedResponseBytes < 1L) {
                throw new IllegalArgumentException(
                        "downstream response limits must be positive"
                );
            }
        }

        public static Limits defaults() {
            return new Limits(
                    10_000,
                    128L * 1024L * 1024L
            );
        }
    }

    private enum ServiceKind {
        DOCUMENT,
        INTERNAL
    }
}