//package com.sparrowx.document.dice.embabel;
//
//import com.embabel.agent.core.DataDictionary;
//import com.embabel.dice.common.SourceAnalysisContext;
//import com.embabel.dice.common.resolver.AlwaysCreateEntityResolver;
//import com.embabel.dice.pipeline.ChunkPropositionResult;
//import com.embabel.dice.pipeline.PropositionPipeline;
//import com.embabel.dice.proposition.Proposition;
//import com.sparrowx.document.dice.DocumentDiceProjectionPort;
//import com.sparrowx.document.domain.models.DocumentEvidenceNode;
//import com.sparrowx.document.domain.models.SourceSpan;
//import com.sparrowx.document.domain.valueobjects.VerificationStatus;
//import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Primary
//@Component
//public class EmbabelDiceDocumentProjectionAdapter implements DocumentDiceProjectionPort {
//
//    private final PropositionPipeline propositionPipeline;
//    private final DataDictionary documentEvidenceDataDictionary;
//
//    public EmbabelDiceDocumentProjectionAdapter(
//            PropositionPipeline propositionPipeline,
//            DataDictionary documentEvidenceDataDictionary
//    ) {
//        this.propositionPipeline = propositionPipeline;
//        this.documentEvidenceDataDictionary = documentEvidenceDataDictionary;
//    }
//
//    @Override
//    public ProjectionResult project(
//            BuildDocumentEvidenceCommand command,
//            List<SourceSpan> sourceSpans
//    ) {
//        if (sourceSpans == null || sourceSpans.isEmpty()) {
//            return new ProjectionResult(
//                    List.of(),
//                    List.of("DICE projection skipped because sourceSpans is empty.")
//            );
//        }
//
//        SourceAnalysisContext context = SourceAnalysisContext
//                .withContextId(contextId(command))
//                .withEntityResolver(AlwaysCreateEntityResolver.INSTANCE)
//                .withSchema(documentEvidenceDataDictionary)
//                .withPromptVariables(promptVariables(command));
//
//        List<DocumentEvidenceNode> nodes = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//
//        for (SourceSpan span : sourceSpans) {
//            if (span == null || span.excerpt() == null || span.excerpt().isBlank()) {
//                warnings.add("Skipped source span without excerpt.");
//                continue;
//            }
//
//            ChunkPropositionResult result = propositionPipeline.processOnce(
//                    span.excerpt(),
//                    sourceId(span),
//                    context
//            );
//
//            if (result == null || result.getPropositions().isEmpty()) {
//                warnings.add("DICE returned no propositions for sourceSpanId=%s".formatted(span.sourceSpanId()));
//                continue;
//            }
//
//            for (Proposition proposition : result.getPropositions()) {
//                nodes.add(toEvidenceNode(command, span, proposition));
//            }
//        }
//
//        if (nodes.isEmpty()) {
//            warnings.add("DICE projection produced no evidence nodes; fallback normalizer may be used.");
//        }
//
//        return new ProjectionResult(nodes, warnings);
//    }
//
//    private DocumentEvidenceNode toEvidenceNode(
//            BuildDocumentEvidenceCommand command,
//            SourceSpan span,
//            Proposition proposition
//    ) {
//        DocumentEvidenceNode.EvidenceNodeType nodeType = defaultNodeType(command);
//
//        return new DocumentEvidenceNode(
//                proposition.getId() == null || proposition.getId().isBlank()
//                        ? UUID.randomUUID().toString()
//                        : proposition.getId(),
//                nodeType,
//                nodeType == DocumentEvidenceNode.EvidenceNodeType.CUSTOM
//                        ? "dice_proposition"
//                        : "",
//                firstNonBlank(span.title(), span.fileName(), "DICE proposition"),
//                firstNonBlank(proposition.getReasoning(), proposition.getText()),
//                proposition.getText(),
//                List.of(span.sourceSpanId()),
//                VerificationStatus.UNVERIFIED,
//                bounded(proposition.getConfidence()),
//                span.excerpt().isBlank() ? 0.0 : 1.0,
//                false,
//                buildTags(command),
//                List.of(),
//                Map.of(
//                        "dice_context_id", contextId(command),
//                        "dice_proposition_id", nullToEmpty(proposition.getId()),
//                        "dice_grounding", String.join(",", proposition.getGrounding()),
//                        "source_span_id", span.sourceSpanId(),
//                        "document_id", span.documentId() == null ? "" : span.documentId().value(),
//                        "chunk_id", span.chunkId() == null ? "" : span.chunkId().value(),
//                        "citation", span.citation()
//                )
//        );
//    }
//
//    private DocumentEvidenceNode.EvidenceNodeType defaultNodeType(
//            BuildDocumentEvidenceCommand command
//    ) {
//        return command.spec().requestedNodeTypes()
//                .stream()
//                .filter(type -> type != null
//                        && type != DocumentEvidenceNode.EvidenceNodeType.UNSPECIFIED)
//                .findFirst()
//                .orElse(DocumentEvidenceNode.EvidenceNodeType.CLAIM);
//    }
//
//    private Map<String, Object> promptVariables(BuildDocumentEvidenceCommand command) {
//        return Map.of(
//                "goal", command.spec().goal().name(),
//                "customGoal", command.spec().customGoal(),
//                "requestedNodeTypes", command.spec().requestedNodeTypes()
//                        .stream()
//                        .map(Enum::name)
//                        .toList(),
//                "requestedRelationTypes", command.spec().requestedRelationTypes()
//                        .stream()
//                        .map(Enum::name)
//                        .toList(),
//                "topics", command.buildContext().topics(),
//                "entityNames", command.buildContext().entityNames(),
//                "keywords", command.buildContext().keywords()
//        );
//    }
//
//    private String contextId(BuildDocumentEvidenceCommand command) {
//        return "document-evidence:%s:%s:%s"
//                .formatted(
//                        command.tenantId() == null ? "tenant" : command.tenantId().value(),
//                        command.projectId() == null ? "project" : command.projectId().value(),
//                        command.requestId() == null ? UUID.randomUUID() : command.requestId().value()
//                );
//    }
//
//    private String sourceId(SourceSpan span) {
//        if (span.sourceSpanId() != null && !span.sourceSpanId().isBlank()) {
//            return span.sourceSpanId();
//        }
//
//        if (span.chunkId() != null) {
//            return "chunk:" + span.chunkId().value();
//        }
//
//        return UUID.randomUUID().toString();
//    }
//
//    private List<String> buildTags(BuildDocumentEvidenceCommand command) {
//        List<String> tags = new ArrayList<>();
//        tags.add("dice");
//        tags.addAll(command.buildContext().topics());
//        tags.addAll(command.buildContext().keywords());
//
//        return tags.stream()
//                .filter(value -> value != null && !value.isBlank())
//                .distinct()
//                .toList();
//    }
//
//    private String firstNonBlank(String... values) {
//        if (values == null) {
//            return "";
//        }
//
//        for (String value : values) {
//            if (value != null && !value.isBlank()) {
//                return value;
//            }
//        }
//
//        return "";
//    }
//
//    private String nullToEmpty(String value) {
//        return value == null ? "" : value;
//    }
//
//    private double bounded(double value) {
//        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
//            return 0.0;
//        }
//
//        return Math.min(1.0, value);
//    }
//}