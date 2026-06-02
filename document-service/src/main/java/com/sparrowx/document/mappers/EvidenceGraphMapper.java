package com.sparrowx.document.mappers;

import com.google.protobuf.Timestamp;
import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.ChunkId;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.proto.DocumentEvidenceEdgeProto;
import com.sparrowx.document.proto.DocumentEvidenceGraphProto;
import com.sparrowx.document.proto.DocumentEvidenceNodeProto;
import com.sparrowx.document.proto.EvidenceGoalProto;
import com.sparrowx.document.proto.EvidenceNodeTypeProto;
import com.sparrowx.document.proto.EvidenceRelationTypeProto;
import com.sparrowx.document.proto.SourceKindProto;
import com.sparrowx.document.proto.SourceSpanProto;
import com.sparrowx.document.proto.VerificationStatusProto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class EvidenceGraphMapper {

    public DocumentEvidenceGraphProto toProto(DocumentEvidenceGraph graph) {
        if (graph == null) {
            return DocumentEvidenceGraphProto.getDefaultInstance();
        }

        DocumentEvidenceGraphProto.Builder builder = DocumentEvidenceGraphProto.newBuilder()
                .setGraphId(nullToEmpty(graph.graphId()))
                .setGoal(toProto(graph.goal()))
                .addAllNodes(graph.nodes().stream().map(this::toProto).toList())
                .addAllEdges(graph.edges().stream().map(this::toProto).toList())
                .addAllSourcePool(graph.sourcePool().stream().map(this::toProto).toList())
                .setVerificationStatus(toProto(graph.verificationStatus()))
                .setConfidence(graph.confidence())
                .setCoverageScore(graph.coverageScore())
                .addAllWarnings(graph.warnings())
                .addAllMissingNodeTypes(graph.missingNodeTypes())
                .setOutputSchemaRef(nullToEmpty(graph.outputSchemaRef()))
                .setOutputSchemaVersion(nullToEmpty(graph.outputSchemaVersion()));

        if (graph.createdAt() != null) {
            builder.setCreatedAt(toTimestamp(graph.createdAt()));
        }

        return builder.build();
    }

    public DocumentEvidenceGraph toDomain(DocumentEvidenceGraphProto proto) {
        if (proto == null || proto.equals(DocumentEvidenceGraphProto.getDefaultInstance())) {
            return null;
        }

        return new DocumentEvidenceGraph(
                proto.getGraphId(),
                toDomain(proto.getGoal()),
                "",
                proto.getNodesList().stream().map(this::toDomain).toList(),
                proto.getEdgesList().stream().map(this::toDomain).toList(),
                proto.getSourcePoolList().stream().map(this::toDomain).toList(),
                toDomain(proto.getVerificationStatus()),
                proto.getConfidence(),
                proto.getCoverageScore(),
                proto.getWarningsList(),
                proto.getMissingNodeTypesList(),
                proto.getOutputSchemaRef(),
                proto.getOutputSchemaVersion(),
                toInstant(proto.getCreatedAt())
        );
    }

    public DocumentEvidenceNodeProto toProto(DocumentEvidenceNode node) {
        if (node == null) {
            return DocumentEvidenceNodeProto.getDefaultInstance();
        }

        DocumentEvidenceNodeProto.Builder builder = DocumentEvidenceNodeProto.newBuilder()
                .setNodeId(nullToEmpty(node.nodeId()))
                .setNodeType(toProto(node.nodeType()))
                .setCustomNodeType(nullToEmpty(node.customNodeType()))
                .setTitle(nullToEmpty(node.title()))
                .setSummary(nullToEmpty(node.summary()))
                .setNormalizedText(nullToEmpty(node.normalizedText()))
                .addAllSourceSpanIds(node.sourceSpanIds())
                .setVerificationStatus(toProto(node.verificationStatus()))
                .setConfidence(node.confidence())
                .setCoverageScore(node.coverageScore())
                .setRequiresSourceContext(node.requiresSourceContext())
                .addAllTags(node.tags())
                .addAllWarnings(node.warnings());

        if (node.attributes() != null && !node.attributes().isEmpty()) {
            builder.putAllAttributes(node.attributes());
        }

        return builder.build();
    }

    public DocumentEvidenceNode toDomain(DocumentEvidenceNodeProto proto) {
        if (proto == null || proto.equals(DocumentEvidenceNodeProto.getDefaultInstance())) {
            return null;
        }

        return new DocumentEvidenceNode(
                proto.getNodeId(),
                toDomain(proto.getNodeType()),
                proto.getCustomNodeType(),
                proto.getTitle(),
                proto.getSummary(),
                proto.getNormalizedText(),
                proto.getSourceSpanIdsList(),
                toDomain(proto.getVerificationStatus()),
                proto.getConfidence(),
                proto.getCoverageScore(),
                proto.getRequiresSourceContext(),
                proto.getTagsList(),
                proto.getWarningsList(),
                proto.getAttributesMap()
        );
    }

    public DocumentEvidenceEdgeProto toProto(DocumentEvidenceEdge edge) {
        if (edge == null) {
            return DocumentEvidenceEdgeProto.getDefaultInstance();
        }

        DocumentEvidenceEdgeProto.Builder builder = DocumentEvidenceEdgeProto.newBuilder()
                .setEdgeId(nullToEmpty(edge.edgeId()))
                .setFromNodeId(nullToEmpty(edge.fromNodeId()))
                .setToNodeId(nullToEmpty(edge.toNodeId()))
                .setRelationType(toProto(edge.relationType()))
                .setCustomRelationType(nullToEmpty(edge.customRelationType()))
                .setRationale(nullToEmpty(edge.rationale()))
                .addAllSourceSpanIds(edge.sourceSpanIds())
                .setConfidence(edge.confidence())
                .addAllWarnings(edge.warnings());

        if (edge.attributes() != null && !edge.attributes().isEmpty()) {
            builder.putAllAttributes(edge.attributes());
        }

        return builder.build();
    }

    public DocumentEvidenceEdge toDomain(DocumentEvidenceEdgeProto proto) {
        if (proto == null || proto.equals(DocumentEvidenceEdgeProto.getDefaultInstance())) {
            return null;
        }

        return new DocumentEvidenceEdge(
                proto.getEdgeId(),
                proto.getFromNodeId(),
                proto.getToNodeId(),
                toDomain(proto.getRelationType()),
                proto.getCustomRelationType(),
                proto.getRationale(),
                proto.getSourceSpanIdsList(),
                proto.getConfidence(),
                proto.getWarningsList(),
                proto.getAttributesMap()
        );
    }

    public SourceSpanProto toProto(SourceSpan span) {
        if (span == null) {
            return SourceSpanProto.getDefaultInstance();
        }

        SourceSpanProto.Builder builder = SourceSpanProto.newBuilder()
                .setSourceSpanId(nullToEmpty(span.sourceSpanId()))
                .setSourceKind(toProto(span.sourceKind()))
                .setDocumentId(value(span.documentId()))
                .setChunkId(value(span.chunkId()))
                .setClaimId(nullToEmpty(span.claimId()))
                .setTitle(nullToEmpty(span.title()))
                .setFileName(nullToEmpty(span.fileName()))
                .setPageStart(span.pageStart())
                .setPageEnd(span.pageEnd())
                .setCitation(nullToEmpty(span.citation()))
                .setExcerpt(nullToEmpty(span.excerpt()))
                .setRelevanceScore(span.relevanceScore());

        Map<String, String> metadata = span.metadata();

        if (metadata != null && !metadata.isEmpty()) {
            builder.putAllMetadata(metadata);
        }

        return builder.build();
    }

    public SourceSpan toDomain(SourceSpanProto proto) {
        if (proto == null || proto.equals(SourceSpanProto.getDefaultInstance())) {
            return null;
        }

        return new SourceSpan(
                proto.getSourceSpanId(),
                toDomain(proto.getSourceKind()),
                documentIdOrNull(proto.getDocumentId()),
                chunkIdOrNull(proto.getChunkId()),
                proto.getClaimId(),
                proto.getTitle(),
                proto.getFileName(),
                proto.getPageStart(),
                proto.getPageEnd(),
                proto.getCitation(),
                proto.getExcerpt(),
                proto.getRelevanceScore(),
                proto.getMetadataMap()
        );
    }

    public EvidenceGoalProto toProto(DocumentEvidenceGraph.EvidenceGoal goal) {
        if (goal == null) {
            return EvidenceGoalProto.EVIDENCE_GOAL_UNSPECIFIED;
        }

        return switch (goal) {
            case EXTRACTION -> EvidenceGoalProto.EVIDENCE_GOAL_EXTRACTION;
            case COMPARISON -> EvidenceGoalProto.EVIDENCE_GOAL_COMPARISON;
            case CONTRADICTION_DETECTION -> EvidenceGoalProto.EVIDENCE_GOAL_CONTRADICTION_DETECTION;
            case COMPLIANCE_AUDIT -> EvidenceGoalProto.EVIDENCE_GOAL_COMPLIANCE_AUDIT;
            case CUSTOM -> EvidenceGoalProto.EVIDENCE_GOAL_CUSTOM;
            case UNSPECIFIED -> EvidenceGoalProto.EVIDENCE_GOAL_UNSPECIFIED;
        };
    }

    public DocumentEvidenceGraph.EvidenceGoal toDomain(EvidenceGoalProto goal) {
        return switch (goal) {
            case EVIDENCE_GOAL_EXTRACTION -> DocumentEvidenceGraph.EvidenceGoal.EXTRACTION;
            case EVIDENCE_GOAL_COMPARISON -> DocumentEvidenceGraph.EvidenceGoal.COMPARISON;
            case EVIDENCE_GOAL_CONTRADICTION_DETECTION -> DocumentEvidenceGraph.EvidenceGoal.CONTRADICTION_DETECTION;
            case EVIDENCE_GOAL_COMPLIANCE_AUDIT -> DocumentEvidenceGraph.EvidenceGoal.COMPLIANCE_AUDIT;
            case EVIDENCE_GOAL_CUSTOM -> DocumentEvidenceGraph.EvidenceGoal.CUSTOM;
            case EVIDENCE_GOAL_UNSPECIFIED, UNRECOGNIZED -> DocumentEvidenceGraph.EvidenceGoal.UNSPECIFIED;
        };
    }

    public EvidenceNodeTypeProto toProto(DocumentEvidenceNode.EvidenceNodeType type) {
        if (type == null) {
            return EvidenceNodeTypeProto.EVIDENCE_NODE_TYPE_UNSPECIFIED;
        }

        return switch (type) {
            case CLAIM -> EvidenceNodeTypeProto.EVIDENCE_NODE_TYPE_CLAIM;
            case ENTITY -> EvidenceNodeTypeProto.EVIDENCE_NODE_TYPE_ENTITY;
            case FRAMEWORK -> EvidenceNodeTypeProto.EVIDENCE_NODE_TYPE_FRAMEWORK;
            case METRIC -> EvidenceNodeTypeProto.EVIDENCE_NODE_TYPE_METRIC;
            case OBLIGATION -> EvidenceNodeTypeProto.EVIDENCE_NODE_TYPE_OBLIGATION;
            case CUSTOM -> EvidenceNodeTypeProto.EVIDENCE_NODE_TYPE_CUSTOM;
            case UNSPECIFIED -> EvidenceNodeTypeProto.EVIDENCE_NODE_TYPE_UNSPECIFIED;
        };
    }

    public DocumentEvidenceNode.EvidenceNodeType toDomain(EvidenceNodeTypeProto type) {
        return switch (type) {
            case EVIDENCE_NODE_TYPE_CLAIM -> DocumentEvidenceNode.EvidenceNodeType.CLAIM;
            case EVIDENCE_NODE_TYPE_ENTITY -> DocumentEvidenceNode.EvidenceNodeType.ENTITY;
            case EVIDENCE_NODE_TYPE_FRAMEWORK -> DocumentEvidenceNode.EvidenceNodeType.FRAMEWORK;
            case EVIDENCE_NODE_TYPE_METRIC -> DocumentEvidenceNode.EvidenceNodeType.METRIC;
            case EVIDENCE_NODE_TYPE_OBLIGATION -> DocumentEvidenceNode.EvidenceNodeType.OBLIGATION;
            case EVIDENCE_NODE_TYPE_CUSTOM -> DocumentEvidenceNode.EvidenceNodeType.CUSTOM;
            case EVIDENCE_NODE_TYPE_UNSPECIFIED, UNRECOGNIZED -> DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED;
        };
    }

    public EvidenceRelationTypeProto toProto(DocumentEvidenceEdge.EvidenceRelationType type) {
        if (type == null) {
            return EvidenceRelationTypeProto.EVIDENCE_RELATION_TYPE_UNSPECIFIED;
        }

        return switch (type) {
            case SUPPORTS -> EvidenceRelationTypeProto.EVIDENCE_RELATION_TYPE_SUPPORTS;
            case CONTRADICTS -> EvidenceRelationTypeProto.EVIDENCE_RELATION_TYPE_CONTRADICTS;
            case MODIFIES -> EvidenceRelationTypeProto.EVIDENCE_RELATION_TYPE_MODIFIES;
            case DEPENDS_ON -> EvidenceRelationTypeProto.EVIDENCE_RELATION_TYPE_DEPENDS_ON;
            case SIMILAR_TO -> EvidenceRelationTypeProto.EVIDENCE_RELATION_TYPE_SIMILAR_TO;
            case CUSTOM -> EvidenceRelationTypeProto.EVIDENCE_RELATION_TYPE_CUSTOM;
            case UNSPECIFIED -> EvidenceRelationTypeProto.EVIDENCE_RELATION_TYPE_UNSPECIFIED;
        };
    }

    public DocumentEvidenceEdge.EvidenceRelationType toDomain(EvidenceRelationTypeProto type) {
        return switch (type) {
            case EVIDENCE_RELATION_TYPE_SUPPORTS -> DocumentEvidenceEdge.EvidenceRelationType.SUPPORTS;
            case EVIDENCE_RELATION_TYPE_CONTRADICTS -> DocumentEvidenceEdge.EvidenceRelationType.CONTRADICTS;
            case EVIDENCE_RELATION_TYPE_MODIFIES -> DocumentEvidenceEdge.EvidenceRelationType.MODIFIES;
            case EVIDENCE_RELATION_TYPE_DEPENDS_ON -> DocumentEvidenceEdge.EvidenceRelationType.DEPENDS_ON;
            case EVIDENCE_RELATION_TYPE_SIMILAR_TO -> DocumentEvidenceEdge.EvidenceRelationType.SIMILAR_TO;
            case EVIDENCE_RELATION_TYPE_CUSTOM -> DocumentEvidenceEdge.EvidenceRelationType.CUSTOM;
            case EVIDENCE_RELATION_TYPE_UNSPECIFIED, UNRECOGNIZED -> DocumentEvidenceEdge.EvidenceRelationType.UNSPECIFIED;
        };
    }

    public VerificationStatusProto toProto(VerificationStatus status) {
        if (status == null) {
            return VerificationStatusProto.VERIFICATION_STATUS_UNSPECIFIED;
        }

        return switch (status) {
            case UNVERIFIED -> VerificationStatusProto.VERIFICATION_STATUS_UNVERIFIED;
            case SUPPORTED -> VerificationStatusProto.VERIFICATION_STATUS_SUPPORTED;
            case PARTIALLY_SUPPORTED -> VerificationStatusProto.VERIFICATION_STATUS_PARTIALLY_SUPPORTED;
            case UNSUPPORTED -> VerificationStatusProto.VERIFICATION_STATUS_UNSUPPORTED;
            case CONTRADICTED -> VerificationStatusProto.VERIFICATION_STATUS_CONTRADICTED;
            case NEEDS_SOURCE_CONTEXT -> VerificationStatusProto.VERIFICATION_STATUS_NEEDS_SOURCE_CONTEXT;
            case UNSPECIFIED -> VerificationStatusProto.VERIFICATION_STATUS_UNSPECIFIED;
        };
    }

    public VerificationStatus toDomain(VerificationStatusProto status) {
        return switch (status) {
            case VERIFICATION_STATUS_UNVERIFIED -> VerificationStatus.UNVERIFIED;
            case VERIFICATION_STATUS_SUPPORTED -> VerificationStatus.SUPPORTED;
            case VERIFICATION_STATUS_PARTIALLY_SUPPORTED -> VerificationStatus.PARTIALLY_SUPPORTED;
            case VERIFICATION_STATUS_UNSUPPORTED -> VerificationStatus.UNSUPPORTED;
            case VERIFICATION_STATUS_CONTRADICTED -> VerificationStatus.CONTRADICTED;
            case VERIFICATION_STATUS_NEEDS_SOURCE_CONTEXT -> VerificationStatus.NEEDS_SOURCE_CONTEXT;
            case VERIFICATION_STATUS_UNSPECIFIED, UNRECOGNIZED -> VerificationStatus.UNSPECIFIED;
        };
    }

    private SourceKindProto toProto(SourceSpan.SourceKind kind) {
        if (kind == null) {
            return SourceKindProto.SOURCE_KIND_UNSPECIFIED;
        }

        return switch (kind) {
            case DOCUMENT_METADATA -> SourceKindProto.SOURCE_KIND_DOCUMENT_METADATA;
            case CHUNK -> SourceKindProto.SOURCE_KIND_CHUNK;
            case CLAIM -> SourceKindProto.SOURCE_KIND_CLAIM;
        };
    }

    private SourceSpan.SourceKind toDomain(SourceKindProto kind) {
        return switch (kind) {
            case SOURCE_KIND_DOCUMENT_METADATA -> SourceSpan.SourceKind.DOCUMENT_METADATA;
            case SOURCE_KIND_CLAIM -> SourceSpan.SourceKind.CLAIM;
            case SOURCE_KIND_CHUNK -> SourceSpan.SourceKind.CHUNK;
            case SOURCE_KIND_UNSPECIFIED, UNRECOGNIZED -> SourceSpan.SourceKind.CHUNK;
        };
    }

    private Timestamp toTimestamp(Instant instant) {
        if (instant == null) {
            return Timestamp.getDefaultInstance();
        }

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null || timestamp.equals(Timestamp.getDefaultInstance())) {
            return Instant.now();
        }

        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    private DocumentId documentIdOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return DocumentId.of(value);
    }

    private ChunkId chunkIdOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return ChunkId.of(value);
    }

    private String value(DocumentId value) {
        return value == null ? "" : value.value();
    }

    private String value(ChunkId value) {
        return value == null ? "" : value.value();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}