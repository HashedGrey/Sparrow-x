package com.sparrowx.document.dice;

import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.models.DocumentEvidenceNode;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class EvidenceGraphPolicy {

    private static final double MIN_INTENT_TERM_MATCH_RATIO = 0.30;
    private static final int MAX_TERMS_IN_WARNING = 8;

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "have", "having", "in", "into", "is", "it", "its", "of",
            "on", "or", "that", "the", "their", "this", "to", "used", "using",
            "was", "were", "with", "show", "shows", "find", "evidence",
            "study", "linking", "linked", "cause", "causes", "caused"
    );

    public PolicyResult evaluate(
            BuildDocumentEvidenceCommand command,
            DocumentEvidenceGraph graph
    ) {
        List<String> warnings = new ArrayList<>();

        if (graph == null) {
            warnings.add("Evidence graph is null.");
            return new PolicyResult(false, warnings);
        }

        if (graph.nodes().isEmpty()) {
            warnings.add("Evidence graph has no nodes.");
        }

        if (command.spec().goal() == DocumentEvidenceGraph.EvidenceGoal.UNSPECIFIED) {
            warnings.add("Evidence goal is unspecified.");
        }

        if (command.spec().goal() == DocumentEvidenceGraph.EvidenceGoal.CUSTOM
                && command.spec().customGoal().isBlank()) {
            warnings.add("Custom evidence goal selected but customGoal is blank.");
        }

        if (graph.verificationStatus() == VerificationStatus.UNSUPPORTED) {
            warnings.add("Evidence graph verification status is unsupported.");
        }

        if (graph.verificationStatus() == VerificationStatus.NEEDS_SOURCE_CONTEXT) {
            warnings.add("Evidence graph verification status needs source context.");
        }

        for (DocumentEvidenceNode node : graph.nodes()) {
            if (node == null) {
                warnings.add("Graph contains null node.");
                continue;
            }

            if (node.sourceSpanIds().isEmpty()) {
                warnings.add("Node %s has no source span references.".formatted(node.nodeId()));
            }

            if (node.normalizedText().isBlank() && node.summary().isBlank()) {
                warnings.add("Node %s has no normalized text or summary.".formatted(node.nodeId()));
            }

            if (node.verificationStatus() == VerificationStatus.UNSUPPORTED) {
                warnings.add("Node %s is unsupported.".formatted(node.nodeId()));
            }

            if (node.verificationStatus() == VerificationStatus.NEEDS_SOURCE_CONTEXT) {
                warnings.add("Node %s needs source context.".formatted(node.nodeId()));
            }
        }

        if (!graph.missingNodeTypes().isEmpty()) {
            warnings.add("Evidence graph is missing requested node types: " + graph.missingNodeTypes());
        }

        IntentRelevanceResult intentRelevance = evaluateIntentRelevance(command, graph);
        warnings.addAll(intentRelevance.warnings());

        boolean acceptable = !graph.nodes().isEmpty()
                && graph.missingNodeTypes().isEmpty()
                && graph.verificationStatus() != VerificationStatus.UNSUPPORTED
                && graph.verificationStatus() != VerificationStatus.NEEDS_SOURCE_CONTEXT
                && intentRelevance.acceptable();

        return new PolicyResult(acceptable, warnings);
    }

    private IntentRelevanceResult evaluateIntentRelevance(
            BuildDocumentEvidenceCommand command,
            DocumentEvidenceGraph graph
    ) {
        List<String> warnings = new ArrayList<>();

        Set<String> intentTerms = buildIntentTerms(command);

        if (intentTerms.isEmpty()) {
            warnings.add("No intent terms were available for evidence relevance checking.");
            return new IntentRelevanceResult(true, 1.0, List.of(), List.of(), warnings);
        }

        String evidenceText = normalizeForMatching(buildEvidenceText(graph));

        List<String> matchedTerms = new ArrayList<>();
        List<String> unmatchedTerms = new ArrayList<>();

        for (String term : intentTerms) {
            if (matchesIntentTerm(evidenceText, term)) {
                matchedTerms.add(term);
            } else {
                unmatchedTerms.add(term);
            }
        }

        double matchRatio = (double) matchedTerms.size() / intentTerms.size();
        int minimumMatches = minimumRequiredMatches(intentTerms.size());

        boolean acceptable = matchedTerms.size() >= minimumMatches
                && matchRatio >= MIN_INTENT_TERM_MATCH_RATIO;

        if (!acceptable) {
            warnings.add(
                    "Evidence graph is source-supported but not relevant to requested evidence intent. " +
                            "Matched intent terms=%s, requiredMinimum=%d, matchRatio=%.2f."
                                    .formatted(matchedTerms, minimumMatches, matchRatio)
            );

            warnings.add(
                    "No source spans directly support enough requested evidence intent terms. " +
                            "Unmatched intent terms include: %s."
                                    .formatted(unmatchedTerms.stream()
                                            .limit(MAX_TERMS_IN_WARNING)
                                            .toList())
            );
        }

        return new IntentRelevanceResult(
                acceptable,
                matchRatio,
                matchedTerms,
                unmatchedTerms,
                warnings
        );
    }

    private Set<String> buildIntentTerms(BuildDocumentEvidenceCommand command) {
        Set<String> terms = new LinkedHashSet<>();

        addStructuredTerms(terms, command.buildContext().topics());
        addStructuredTerms(terms, command.buildContext().entityNames());
        addStructuredTerms(terms, command.buildContext().keywords());

        addStructuredTerms(terms, List.of(command.buildContext().retrievalHint()));
        addStructuredTerms(terms, List.of(command.spec().customGoal()));

        Map<String, String> options = command.spec().options();
        if (options != null) {
            addStructuredTerms(terms, List.of(options.get("focus")));
        }

        return terms;
    }

    private void addStructuredTerms(Set<String> terms, List<String> values) {
        if (values == null) {
            return;
        }

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }

            String normalized = normalizeForMatching(value);

            if (normalized.isBlank()) {
                continue;
            }

            if (isUsefulIntentPhrase(normalized)) {
                terms.add(normalized);
            }

            for (String token : normalized.split("\\s+")) {
                if (isUsefulIntentToken(token)) {
                    terms.add(token);
                }
            }
        }
    }

    private boolean matchesIntentTerm(String evidenceText, String intentTerm) {
        if (evidenceText == null || evidenceText.isBlank()) {
            return false;
        }

        if (intentTerm == null || intentTerm.isBlank()) {
            return false;
        }

        String normalizedTerm = normalizeForMatching(intentTerm);

        if (normalizedTerm.isBlank()) {
            return false;
        }

        if (evidenceText.contains(normalizedTerm)) {
            return true;
        }

        String[] tokens = normalizedTerm.split("\\s+");

        if (tokens.length <= 1) {
            return false;
        }

        int matchedTokens = 0;
        int usefulTokens = 0;

        for (String token : tokens) {
            if (!isUsefulIntentToken(token)) {
                continue;
            }

            usefulTokens++;

            if (evidenceText.contains(token)) {
                matchedTokens++;
            }
        }

        return usefulTokens > 0 && matchedTokens == usefulTokens;
    }

    private String buildEvidenceText(DocumentEvidenceGraph graph) {
        StringBuilder builder = new StringBuilder();

        for (SourceSpan span : graph.sourcePool()) {
            if (span == null) {
                continue;
            }

            append(builder, span.title());
            append(builder, span.fileName());
            append(builder, span.citation());
            append(builder, span.excerpt());
        }

        for (DocumentEvidenceNode node : graph.nodes()) {
            if (node == null) {
                continue;
            }

            append(builder, node.title());
            append(builder, node.summary());
            append(builder, node.normalizedText());

            for (String tag : node.tags()) {
                append(builder, tag);
            }

            for (String warning : node.warnings()) {
                append(builder, warning);
            }

            if (node.attributes() != null) {
                for (Map.Entry<String, String> entry : node.attributes().entrySet()) {
                    append(builder, entry.getKey());
                    append(builder, entry.getValue());
                }
            }
        }

        return builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        builder.append(' ').append(value);
    }

    private String normalizeForMatching(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+\\-\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isUsefulIntentPhrase(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String[] tokens = value.split("\\s+");

        int usefulTokenCount = 0;

        for (String token : tokens) {
            if (isUsefulIntentToken(token)) {
                usefulTokenCount++;
            }
        }

        return usefulTokenCount >= 2;
    }

    private boolean isUsefulIntentToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String normalized = normalizeForMatching(token);

        if (normalized.length() < 3) {
            return false;
        }

        return !STOP_WORDS.contains(normalized);
    }

    private int minimumRequiredMatches(int intentTermCount) {
        if (intentTermCount <= 2) {
            return 1;
        }

        if (intentTermCount <= 5) {
            return 2;
        }

        return Math.max(3, (int) Math.ceil(intentTermCount * MIN_INTENT_TERM_MATCH_RATIO));
    }

    public record PolicyResult(
            boolean acceptable,
            List<String> warnings
    ) {
        public PolicyResult {
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }

    private record IntentRelevanceResult(
            boolean acceptable,
            double matchRatio,
            List<String> matchedTerms,
            List<String> unmatchedTerms,
            List<String> warnings
    ) {
        private IntentRelevanceResult {
            matchedTerms = matchedTerms == null ? List.of() : List.copyOf(matchedTerms);
            unmatchedTerms = unmatchedTerms == null ? List.of() : List.copyOf(unmatchedTerms);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }
}