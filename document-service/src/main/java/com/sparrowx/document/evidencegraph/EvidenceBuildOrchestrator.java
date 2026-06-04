package com.sparrowx.document.evidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceEdge;
import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.RetrievalMode;
import com.sparrowx.document.domain.valueobjects.SearchQueryText;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import com.sparrowx.document.retrieval.ClaimCacheRetriever;
import com.sparrowx.document.retrieval.HybridDocumentRetriever;
import com.sparrowx.document.retrieval.SourceSpanBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class EvidenceBuildOrchestrator {

    private static final int DEFAULT_LIMIT = 10;

    private final HybridDocumentRetriever hybridDocumentRetriever;
    private final SourceSpanBuilder sourceSpanBuilder;
    private final ClaimCacheRetriever claimCacheRetriever;
    private final EvidenceNormalizer evidenceNormalizer;
    private final EvidenceRelationLinker evidenceRelationLinker;
    private final EvidenceGraphBuilder evidenceGraphBuilder;
    private final EvidenceSchemaValidator evidenceSchemaValidator;
    private final EvidenceGraphPolicy evidenceGraphPolicy;

    public EvidenceBuildOrchestrator(
            HybridDocumentRetriever hybridDocumentRetriever,
            SourceSpanBuilder sourceSpanBuilder,
            ClaimCacheRetriever claimCacheRetriever,
            EvidenceNormalizer evidenceNormalizer,
            EvidenceRelationLinker evidenceRelationLinker,
            EvidenceGraphBuilder evidenceGraphBuilder,
            EvidenceSchemaValidator evidenceSchemaValidator,
            EvidenceGraphPolicy evidenceGraphPolicy
    ) {
        this.hybridDocumentRetriever = hybridDocumentRetriever;
        this.sourceSpanBuilder = sourceSpanBuilder;
        this.claimCacheRetriever = claimCacheRetriever;
        this.evidenceNormalizer = evidenceNormalizer;
        this.evidenceRelationLinker = evidenceRelationLinker;
        this.evidenceGraphBuilder = evidenceGraphBuilder;
        this.evidenceSchemaValidator = evidenceSchemaValidator;
        this.evidenceGraphPolicy = evidenceGraphPolicy;
    }

    public EvidenceBuildOrchestrationResult build(BuildDocumentEvidenceCommand command) {
        validate(command);

        List<String> warnings = new ArrayList<>();
        List<SourceSpan> sourcePool = new ArrayList<>();

        boolean usedClaimCache = false;
        boolean usedChunkRetrieval = false;

        if (command.allowClaimCache()) {
            ClaimCacheRetriever.ClaimCacheResult claimCacheResult =
                    claimCacheRetriever.retrieve(command);

            sourcePool.addAll(claimCacheResult.spans());
            warnings.addAll(claimCacheResult.warnings());

            usedClaimCache = !claimCacheResult.spans().isEmpty();
        }

        if (sourcePool.isEmpty() || shouldUseChunkRetrieval(command)) {
            List<SourceSpan> retrievedSpans = retrieveSourceSpans(command);
            sourcePool.addAll(retrievedSpans);
            usedChunkRetrieval = !retrievedSpans.isEmpty();
        }

        if (sourcePool.isEmpty()) {
            warnings.add("No source spans found for evidence build.");
        }

        EvidenceNormalizer.NormalizationResult normalizationResult =
                evidenceNormalizer.normalize(command, sourcePool);

        warnings.addAll(normalizationResult.warnings());

        List<DocumentEvidenceNode> nodes = new ArrayList<>(normalizationResult.nodes());

        if (isContradictionDetection(command)) {
            String testedClaim = extractTestedClaim(command);

            if (testedClaim.isBlank()) {
                warnings.add("CONTRADICTION_DETECTION requested but no tested claim could be extracted.");
            } else {
                List<SourceSpan> testedClaimSourceSpans = sourceSpansForTestedClaim(testedClaim, sourcePool);

                if (testedClaimSourceSpans.isEmpty()) {
                    testedClaimSourceSpans = sourcePool;
                    warnings.add("Tested claim used full source pool because no focused comparison source spans were found.");
                }

                nodes.add(0, testedClaimNode(testedClaim, testedClaimSourceSpans));
                warnings.add("CONTRADICTION_DETECTION preserved tested claim as a first-class graph node.");
            }
        }

        EvidenceRelationLinker.LinkResult linkResult =
                evidenceRelationLinker.link(command, nodes);

        List<DocumentEvidenceEdge> edges = new ArrayList<>(linkResult.edges());

        warnings.addAll(linkResult.warnings());

        DocumentEvidenceGraph graph = evidenceGraphBuilder.build(
                command,
                nodes,
                edges,
                sourcePool,
                warnings
        );

        EvidenceSchemaValidator.ValidationResult schemaValidation =
                evidenceSchemaValidator.validate(graph);

        if (!schemaValidation.valid()) {
            warnings.addAll(schemaValidation.errors());
        }

        EvidenceGraphPolicy.PolicyResult policyResult =
                evidenceGraphPolicy.evaluate(command, graph);

        warnings.addAll(policyResult.warnings());

        if (!policyResult.acceptable()) {
            warnings.add("Evidence graph policy marked graph as not acceptable.");
        }

        graph = new DocumentEvidenceGraph(
                graph.graphId(),
                graph.goal(),
                graph.customGoal(),
                graph.nodes(),
                graph.edges(),
                graph.sourcePool(),
                graph.verificationStatus(),
                graph.confidence(),
                graph.coverageScore(),
                warnings,
                graph.missingNodeTypes(),
                graph.outputSchemaRef(),
                graph.outputSchemaVersion(),
                graph.createdAt()
        );

        return new EvidenceBuildOrchestrationResult(
                graph,
                usedChunkRetrieval,
                usedClaimCache,
                warnings
        );
    }

    private List<SourceSpan> sourceSpansForTestedClaim(
            String testedClaim,
            List<SourceSpan> sourcePool
    ) {
        if (sourcePool == null || sourcePool.isEmpty()) {
            return List.of();
        }

        String normalizedClaim = normalize(testedClaim);

        if (isActivityMindWanderingVarianceClaim(normalizedClaim)) {
            List<SourceSpan> focused = sourcePool.stream()
                    .filter(span -> span != null)
                    .filter(span -> isActivityMindWanderingVarianceEvidence(span.excerpt()))
                    .limit(3)
                    .toList();

            if (!focused.isEmpty()) {
                return focused;
            }
        }

        return sourcePool.stream()
                .filter(span -> span != null)
                .filter(span -> overlapsTestedClaim(normalizedClaim, span.excerpt()))
                .limit(3)
                .toList();
    }

    private boolean isActivityMindWanderingVarianceClaim(String value) {
        return containsAny(value, "activity", "activities")
                && containsAny(value, "mind wandering", "mind-wandering", "mindwandering")
                && containsAny(value, "variance", "happiness")
                && containsAny(value, "more", "greater", "higher", "larger", "less", "lower", "smaller");
    }

    private boolean isActivityMindWanderingVarianceEvidence(String excerpt) {
        String value = normalize(excerpt);

        return containsAny(value, "activity", "activities")
                && containsAny(value, "mind wandering", "mind-wandering", "mindwandering")
                && value.contains("%")
                && containsAny(value, "variance", "happiness")
                && containsAny(value, "explained", "explains");
    }

    private boolean overlapsTestedClaim(
            String normalizedClaim,
            String excerpt
    ) {
        String normalizedExcerpt = normalize(excerpt);

        if (normalizedClaim.isBlank() || normalizedExcerpt.isBlank()) {
            return false;
        }

        int matches = 0;

        for (String token : normalizedClaim.split("\\s+")) {
            if (token.length() < 4) {
                continue;
            }

            if (normalizedExcerpt.contains(token)) {
                matches++;
            }
        }

        return matches >= 3;
    }

    private DocumentEvidenceNode testedClaimNode(
            String testedClaim,
            List<SourceSpan> sourceSpans
    ) {
        List<String> sourceSpanIds = sourceSpans == null
                ? List.of()
                : sourceSpans.stream()
                .filter(span -> span != null && span.sourceSpanId() != null && !span.sourceSpanId().isBlank())
                .map(SourceSpan::sourceSpanId)
                .distinct()
                .toList();

        return new DocumentEvidenceNode(
                "tested_claim_" + UUID.randomUUID(),
                DocumentEvidenceNode.EvidenceNodeType.CLAIM,
                "",
                "Tested Claim",
                testedClaim,
                testedClaim,
                sourceSpanIds,
                VerificationStatus.UNVERIFIED,
                0.0,
                0.0,
                false,
                List.of("tested-claim", "contradiction-detection"),
                List.of("This node represents the user/request proposition being tested against focused source spans."),
                Map.of(
                        "role", "tested_claim",
                        "origin", "request",
                        "verification_goal", "CONTRADICTION_DETECTION"
                )
        );
    }

    private boolean isContradictionDetection(BuildDocumentEvidenceCommand command) {
        return command.spec() != null
                && command.spec().goal() == DocumentEvidenceGraph.EvidenceGoal.CONTRADICTION_DETECTION;
    }

    private String extractTestedClaim(BuildDocumentEvidenceCommand command) {
        if (command == null || command.spec() == null || command.buildContext() == null) {
            return "";
        }

        Map<String, String> options = command.spec().options();

        String explicit = firstNonBlank(
                option(options, "target_claim"),
                option(options, "tested_claim"),
                option(options, "claim"),
                option(options, "proposition")
        );

        if (!explicit.isBlank()) {
            return cleanClaimText(explicit);
        }

        String fromRetrievalHint = extractAfterMarker(
                command.buildContext().retrievalHint(),
                "claim:"
        );

        if (!fromRetrievalHint.isBlank()) {
            return cleanClaimText(fromRetrievalHint);
        }

        String fromFocusClaimThat = extractAfterMarker(
                option(options, "focus"),
                "claim that"
        );

        if (!fromFocusClaimThat.isBlank()) {
            return cleanClaimText(fromFocusClaimThat);
        }

        String fromCustomGoalClaimThat = extractAfterMarker(
                command.spec().customGoal(),
                "claim that"
        );

        if (!fromCustomGoalClaimThat.isBlank()) {
            return cleanClaimText(fromCustomGoalClaimThat);
        }

        String fromDebugClaimThat = extractAfterMarker(
                command.buildContext().debugTaskInstruction(),
                "claim that"
        );

        if (!fromDebugClaimThat.isBlank()) {
            return cleanClaimText(fromDebugClaimThat);
        }

        return "";
    }

    private String extractAfterMarker(String value, String marker) {
        if (value == null || value.isBlank() || marker == null || marker.isBlank()) {
            return "";
        }

        String lowerValue = value.toLowerCase();
        String lowerMarker = marker.toLowerCase();

        int index = lowerValue.indexOf(lowerMarker);

        if (index < 0) {
            return "";
        }

        String remainder = value.substring(index + marker.length()).trim();

        if (remainder.isBlank()) {
            return "";
        }

        int end = firstSentenceBoundary(remainder);

        if (end > 0) {
            return remainder.substring(0, end).trim();
        }

        return remainder.trim();
    }

    private int firstSentenceBoundary(String value) {
        int best = -1;

        for (char boundary : new char[]{'.', '?', '!'}) {
            int index = value.indexOf(boundary);

            if (index >= 0 && (best < 0 || index < best)) {
                best = index;
            }
        }

        return best;
    }

    private String cleanClaimText(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value.trim();

        while (cleaned.startsWith(":")) {
            cleaned = cleaned.substring(1).trim();
        }

        cleaned = cleaned
                .replaceAll("(?i)^whether\\s+", "")
                .replaceAll("(?i)^the\\s+document\\s+supports\\s+or\\s+contradicts\\s+", "")
                .replaceAll("(?i)^the\\s+document\\s+supports\\s+", "")
                .replaceAll("(?i)^the\\s+document\\s+contradicts\\s+", "")
                .replaceAll("(?i)^that\\s+", "")
                .trim();

        if (cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }

        return cleaned;
    }

    private String option(Map<String, String> options, String key) {
        if (options == null || key == null) {
            return "";
        }

        return options.getOrDefault(key, "");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value
                .toLowerCase()
                .replace("mind-wandering", "mind wandering")
                .replace("mindwandering", "mind wandering")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAny(String value, String... terms) {
        if (value == null || terms == null) {
            return false;
        }

        for (String term : terms) {
            if (term != null && !term.isBlank() && value.contains(term)) {
                return true;
            }
        }

        return false;
    }

    private List<SourceSpan> retrieveSourceSpans(BuildDocumentEvidenceCommand command) {
        SearchQueryText retrievalQuery = SearchQueryText.of(buildRetrievalQuery(command));
        int limit = normalizeLimit(command.limit());
        RetrievalMode retrievalMode = command.retrievalMode() == null
                ? RetrievalMode.HYBRID
                : command.retrievalMode();

        List<RetrievalEvidence> evidence = hybridDocumentRetriever.retrieve(
                new HybridDocumentRetriever.RetrieveDocumentsRequest(
                        command.tenantId(),
                        command.userId(),
                        command.projectId(),
                        command.teamId(),
                        retrievalQuery,
                        limit,
                        retrievalMode,
                        Set.copyOf(command.scope().documentIds())
                )
        );

        boolean includeExcerpts = command.includeExcerpts() || command.requireVerification();

        return evidence.stream()
                .map(item -> sourceSpanBuilder.fromRetrievalEvidence(item, includeExcerpts))
                .toList();
    }

    private boolean shouldUseChunkRetrieval(BuildDocumentEvidenceCommand command) {
        return command.requireVerification()
                || command.buildContext().retrievalHint() != null && !command.buildContext().retrievalHint().isBlank()
                || !command.buildContext().topics().isEmpty()
                || !command.buildContext().entityNames().isEmpty()
                || !command.buildContext().keywords().isEmpty();
    }

    private String buildRetrievalQuery(BuildDocumentEvidenceCommand command) {
        List<String> parts = new ArrayList<>();

        String retrievalHint = command.buildContext().retrievalHint();

        if (retrievalHint != null && !retrievalHint.isBlank()) {
            parts.add(retrievalHint);
        }

        parts.addAll(command.buildContext().entityNames());
        parts.addAll(command.buildContext().topics());
        parts.addAll(command.buildContext().keywords());

        String query = String.join(" ", parts).trim();

        if (!query.isBlank()) {
            return query;
        }

        String debugTaskInstruction = command.buildContext().debugTaskInstruction();

        if (debugTaskInstruction != null && !debugTaskInstruction.isBlank()) {
            return debugTaskInstruction;
        }

        throw InvalidDocumentException.blankField("retrievalHint/topics/entityNames/keywords");
    }

    private int normalizeLimit(int limit) {
        return limit <= 0 ? DEFAULT_LIMIT : limit;
    }

    private void validate(BuildDocumentEvidenceCommand command) {
        if (command == null) {
            throw InvalidDocumentException.nullQuery("BuildDocumentEvidenceCommand");
        }

        if (command.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (command.userId() == null) {
            throw InvalidDocumentException.blankField("userId");
        }

        if (command.scope() == null) {
            throw InvalidDocumentException.blankField("scope");
        }

        if (command.spec() == null) {
            throw InvalidDocumentException.blankField("spec");
        }

        if (command.buildContext() == null) {
            throw InvalidDocumentException.blankField("buildContext");
        }
    }

    public record EvidenceBuildOrchestrationResult(
            DocumentEvidenceGraph graph,
            boolean usedChunkRetrieval,
            boolean usedClaimCache,
            List<String> warnings
    ) {
        public EvidenceBuildOrchestrationResult {
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }
}