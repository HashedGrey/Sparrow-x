package com.sparrowx.agentic.tools.document;

import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.mission.evidence.EvidenceSourceType;
import com.sparrowx.document.proto.BuildDocumentEvidenceResponse;
import com.sparrowx.document.proto.DocumentEvidenceGraphProto;
import com.sparrowx.document.proto.SearchDocumentSpansResponse;
import com.sparrowx.document.proto.SourceKindProto;
import com.sparrowx.document.proto.SourceSpanProto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DocumentEvidenceMapper {

    private static final String DOCUMENT_SERVICE = "document-service";

    public List<EvidenceRef> fromSearch(SearchDocumentSpansResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("response must not be null");
        }
        return deduplicate(response.getSpansList().stream().map(this::fromSpan).toList());
    }

    public List<EvidenceRef> fromBuild(BuildDocumentEvidenceResponse response) {
        if (response == null || !response.hasGraph()) {
            throw new IllegalArgumentException("document evidence response must contain a graph");
        }

        DocumentEvidenceGraphProto graph = response.getGraph();
        List<EvidenceRef> evidence = new ArrayList<>();
        evidence.add(fromGraph(graph));
        graph.getSourcePoolList().stream().map(this::fromSpan).forEach(evidence::add);
        return deduplicate(evidence);
    }

    public EvidenceRef fromSpan(SourceSpanProto span) {
        if (span == null || span.getSourceSpanId().isBlank()) {
            throw new IllegalArgumentException("source span id must not be blank");
        }

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.putAll(span.getMetadataMap());
        attributes.put("sourceKind", span.getSourceKind().name());
        attributes.put("claimId", span.getClaimId());
        attributes.put("title", span.getTitle());
        attributes.put("fileName", span.getFileName());
        attributes.put("excerpt", span.getExcerpt());
        attributes.put("relevanceScore", span.getRelevanceScore());

        return new EvidenceRef(
                "document-span:" + span.getSourceSpanId(),
                sourceType(span.getSourceKind()),
                DOCUMENT_SERVICE,
                span.getSourceSpanId(),
                firstPresent(span.getMetadataMap(), "source_uri", "object_uri", "uri"),
                span.getMetadataMap().getOrDefault("artifact_id", ""),
                span.getDocumentId(),
                "",
                firstNonBlank(span.getCitation(), span.getTitle(), span.getFileName()),
                span.getPageStart(),
                span.getPageEnd(),
                span.getMetadataMap().getOrDefault("section", ""),
                span.getChunkId(),
                span.getMetadataMap().getOrDefault("sha256", ""),
                attributes);
    }

    public EvidenceRef fromGraph(DocumentEvidenceGraphProto graph) {
        if (graph == null || graph.getGraphId().isBlank()) {
            throw new IllegalArgumentException("graph id must not be blank");
        }

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("goal", graph.getGoal().name());
        attributes.put("verificationStatus", graph.getVerificationStatus().name());
        attributes.put("confidence", graph.getConfidence());
        attributes.put("coverageScore", graph.getCoverageScore());
        attributes.put("warnings", List.copyOf(graph.getWarningsList()));
        attributes.put("missingNodeTypes", List.copyOf(graph.getMissingNodeTypesList()));
        attributes.put("outputSchemaRef", graph.getOutputSchemaRef());
        attributes.put("outputSchemaVersion", graph.getOutputSchemaVersion());
        attributes.put(
                "nodeIds",
                graph.getNodesList().stream().map(node -> node.getNodeId()).toList());
        attributes.put(
                "edgeIds",
                graph.getEdgesList().stream().map(edge -> edge.getEdgeId()).toList());

        return new EvidenceRef(
                "document-graph:" + graph.getGraphId(),
                EvidenceSourceType.TOOL_RESULT,
                DOCUMENT_SERVICE,
                graph.getGraphId(),
                "",
                "",
                graph.getGraphId(),
                "",
                "Document evidence graph",
                0,
                0,
                "",
                "",
                "",
                attributes);
    }

    private static EvidenceSourceType sourceType(SourceKindProto sourceKind) {
        return switch (sourceKind) {
            case SOURCE_KIND_DOCUMENT_METADATA -> EvidenceSourceType.DOCUMENT;
            case SOURCE_KIND_CHUNK, SOURCE_KIND_CLAIM,
                 SOURCE_KIND_UNSPECIFIED, UNRECOGNIZED -> EvidenceSourceType.DOCUMENT_SPAN;
        };
    }

    private static List<EvidenceRef> deduplicate(List<EvidenceRef> evidence) {
        Map<String, EvidenceRef> byId = new LinkedHashMap<>();
        evidence.forEach(item -> byId.putIfAbsent(item.evidenceId(), item));
        return List.copyOf(byId.values());
    }

    private static String firstPresent(Map<String, String> values, String... keys) {
        for (String key : keys) {
            String value = values.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
