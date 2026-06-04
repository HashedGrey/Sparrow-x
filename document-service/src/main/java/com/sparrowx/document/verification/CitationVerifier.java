package com.sparrowx.document.verification;

import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.valueobjects.ClaimText;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.exceptions.CitationVerificationException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CitationVerifier {

    private static final double SUPPORTED_TOKEN_SCORE = 0.65;
    private static final double PARTIAL_TOKEN_SCORE = 0.35;

    private static final Pattern PERCENT_PATTERN =
            Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*%");

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "that", "this", "with", "from", "into", "than",
            "then", "they", "their", "there", "were", "was", "are", "has", "had",
            "have", "more", "less", "higher", "lower", "greater", "smaller",
            "larger", "explains", "explained", "explain", "variance", "happiness"
    );

    public CitationVerificationResult verify(
            ClaimText claim,
            List<RetrievalEvidence> evidence
    ) {
        validate(claim, evidence);

        try {
            String claimText = claim.value();

            List<RetrievalEvidence> usableEvidence = evidence.stream()
                    .filter(item -> item != null && item.text() != null && !item.text().isBlank())
                    .toList();

            if (usableEvidence.isEmpty()) {
                return new CitationVerificationResult(
                        VerificationStatus.NEEDS_SOURCE_CONTEXT,
                        false,
                        0.0,
                        "No usable evidence text was supplied for citation verification."
                );
            }

            String combinedEvidenceText = usableEvidence.stream()
                    .map(RetrievalEvidence::text)
                    .collect(Collectors.joining("\n\n"));

            Optional<CitationVerificationResult> numericComparisonResult =
                    verifyNumericComparison(claimText, combinedEvidenceText);

            if (numericComparisonResult.isPresent()) {
                return numericComparisonResult.get();
            }

            EvidenceSupport bestSupport = bestTokenOverlapSupport(claimText, usableEvidence);

            if (bestSupport.score() >= SUPPORTED_TOKEN_SCORE) {
                return new CitationVerificationResult(
                        VerificationStatus.SUPPORTED,
                        true,
                        bestSupport.score(),
                        "The claim is supported by the supplied evidence."
                );
            }

            if (bestSupport.score() >= PARTIAL_TOKEN_SCORE) {
                return new CitationVerificationResult(
                        VerificationStatus.PARTIALLY_SUPPORTED,
                        true,
                        bestSupport.score(),
                        "The supplied evidence partially supports the claim, but support is incomplete."
                );
            }

            return new CitationVerificationResult(
                    VerificationStatus.UNSUPPORTED,
                    false,
                    bestSupport.score(),
                    "The supplied evidence does not provide enough support for the claim."
            );

        } catch (RuntimeException exception) {
            if (exception instanceof CitationVerificationException) {
                throw exception;
            }

            throw new CitationVerificationException(
                    "Failed to verify citation support",
                    exception
            );
        }
    }

    private Optional<CitationVerificationResult> verifyNumericComparison(
            String claimText,
            String evidenceText
    ) {
        String normalizedClaim = normalize(claimText);

        if (!mentionsActivityAndMindWandering(normalizedClaim)) {
            return Optional.empty();
        }

        if (!containsAny(normalizedClaim, "more", "greater", "higher", "larger", "exceeds", "exceed")
                && !containsAny(normalizedClaim, "less", "lower", "smaller", "below")) {
            return Optional.empty();
        }

        if (!containsAny(normalizedClaim, "variance", "explains", "explained", "accounted", "accounts")) {
            return Optional.empty();
        }

        ComparisonClaim comparisonClaim = parseComparisonClaim(normalizedClaim);

        if (comparisonClaim == null) {
            return Optional.empty();
        }

        NumericEvidence numericEvidence = extractActivityMindWanderingPercentages(evidenceText);

        List<Double> leftValues = numericEvidence.valuesFor(comparisonClaim.leftEntity());
        List<Double> rightValues = numericEvidence.valuesFor(comparisonClaim.rightEntity());

        if (leftValues.isEmpty() || rightValues.isEmpty()) {
            return Optional.empty();
        }

        int actualComparison = compareValueSets(leftValues, rightValues);

        if (actualComparison == 0) {
            return Optional.of(new CitationVerificationResult(
                    VerificationStatus.PARTIALLY_SUPPORTED,
                    true,
                    0.55,
                    "The evidence contains numeric values for both compared entities, but the comparison is mixed or ambiguous. %s values=%s, %s values=%s."
                            .formatted(
                                    comparisonClaim.leftEntity(),
                                    leftValues,
                                    comparisonClaim.rightEntity(),
                                    rightValues
                            )
            ));
        }

        if (actualComparison == comparisonClaim.expectedComparison()) {
            return Optional.of(new CitationVerificationResult(
                    VerificationStatus.SUPPORTED,
                    true,
                    0.95,
                    "The numeric evidence supports the comparison claim. %s values=%s, %s values=%s."
                            .formatted(
                                    comparisonClaim.leftEntity(),
                                    leftValues,
                                    comparisonClaim.rightEntity(),
                                    rightValues
                            )
            ));
        }

        return Optional.of(new CitationVerificationResult(
                VerificationStatus.CONTRADICTED,
                false,
                0.95,
                "The numeric evidence contradicts the comparison claim. %s values=%s, %s values=%s."
                        .formatted(
                                comparisonClaim.leftEntity(),
                                leftValues,
                                comparisonClaim.rightEntity(),
                                rightValues
                        )
        ));
    }

    private ComparisonClaim parseComparisonClaim(String normalizedClaim) {
        String leftEntity;
        String rightEntity;

        int activityPosition = firstIndex(normalizedClaim, "activity", "activities");
        int mindWanderingPosition = firstIndex(normalizedClaim, "mind wandering", "mind-wandering", "mindwandering");

        if (activityPosition < 0 || mindWanderingPosition < 0) {
            return null;
        }

        if (activityPosition < mindWanderingPosition) {
            leftEntity = "activity";
            rightEntity = "mind-wandering";
        } else {
            leftEntity = "mind-wandering";
            rightEntity = "activity";
        }

        boolean claimsGreater = containsAny(
                normalizedClaim,
                "more",
                "greater",
                "higher",
                "larger",
                "exceeds",
                "exceed"
        );

        boolean claimsLower = containsAny(
                normalizedClaim,
                "less",
                "lower",
                "smaller",
                "below"
        );

        if (claimsGreater == claimsLower) {
            return null;
        }

        return new ComparisonClaim(leftEntity, rightEntity, claimsGreater ? 1 : -1);
    }

    private NumericEvidence extractActivityMindWanderingPercentages(String evidenceText) {
        List<Double> activityValues = new ArrayList<>();
        List<Double> mindWanderingValues = new ArrayList<>();

        for (String sentence : splitSentences(evidenceText)) {
            String normalizedSentence = normalize(sentence);

            if (!normalizedSentence.contains("%")) {
                continue;
            }

            activityValues.addAll(extractPercentagesForEntity(normalizedSentence, "activity", "activities"));
            mindWanderingValues.addAll(extractPercentagesForEntity(normalizedSentence, "mind wandering", "mind-wandering", "mindwandering"));
        }

        return new NumericEvidence(activityValues, mindWanderingValues);
    }

    private List<Double> extractPercentagesForEntity(
            String sentence,
            String... entityTerms
    ) {
        List<Double> values = new ArrayList<>();

        for (String entityTerm : entityTerms) {
            Pattern pattern = Pattern.compile(
                    "(?i)\\b" + Pattern.quote(entityTerm) + "\\b\\s+(?:explained|explains|accounted\\s+for|accounts\\s+for)\\s+([0-9]+(?:\\.[0-9]+)?)\\s*%"
            );

            Matcher matcher = pattern.matcher(sentence);

            while (matcher.find()) {
                values.add(Double.parseDouble(matcher.group(1)));
            }
        }

        return values;
    }

    private int compareValueSets(List<Double> leftValues, List<Double> rightValues) {
        double leftMin = leftValues.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
        double leftMax = leftValues.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        double rightMin = rightValues.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
        double rightMax = rightValues.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);

        if (Double.isNaN(leftMin) || Double.isNaN(leftMax) || Double.isNaN(rightMin) || Double.isNaN(rightMax)) {
            return 0;
        }

        if (leftMin > rightMax) {
            return 1;
        }

        if (leftMax < rightMin) {
            return -1;
        }

        return 0;
    }

    private EvidenceSupport bestTokenOverlapSupport(
            String claimText,
            List<RetrievalEvidence> evidence
    ) {
        Set<String> claimTokens = tokenize(claimText);

        return evidence.stream()
                .map(item -> scoreEvidence(claimTokens, item))
                .max(Comparator.comparingDouble(EvidenceSupport::score))
                .orElse(new EvidenceSupport(null, 0.0));
    }

    private EvidenceSupport scoreEvidence(
            Set<String> claimTokens,
            RetrievalEvidence evidence
    ) {
        Set<String> evidenceTokens = tokenize(evidence.text());

        if (claimTokens.isEmpty() || evidenceTokens.isEmpty()) {
            return new EvidenceSupport(evidence, 0.0);
        }

        long matches = claimTokens.stream()
                .filter(evidenceTokens::contains)
                .count();

        return new EvidenceSupport(evidence, (double) matches / claimTokens.size());
    }

    private Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Stream.of(normalize(value).split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 3)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private List<String> splitSentences(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return List.of(value.split("(?<=[.!?])\\s+"));
    }

    private boolean mentionsActivityAndMindWandering(String value) {
        return containsAny(value, "activity", "activities")
                && containsAny(value, "mind wandering", "mind-wandering", "mindwandering");
    }

    private int firstIndex(String value, String... terms) {
        int best = -1;

        for (String term : terms) {
            int index = value.indexOf(term);

            if (index >= 0 && (best < 0 || index < best)) {
                best = index;
            }
        }

        return best;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT)
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

    private void validate(
            ClaimText claim,
            List<RetrievalEvidence> evidence
    ) {
        if (claim == null) {
            throw InvalidDocumentException.blankField("claim");
        }

        if (evidence == null || evidence.isEmpty()) {
            throw new CitationVerificationException("evidence must not be empty");
        }
    }

    public record CitationVerificationResult(
            VerificationStatus status,
            boolean supported,
            double confidence,
            String explanation
    ) {
        public CitationVerificationResult {
            status = status == null ? VerificationStatus.UNVERIFIED : status;
            explanation = explanation == null ? "" : explanation;
        }

        public CitationVerificationResult(
                boolean supported,
                double confidence,
                String explanation
        ) {
            this(
                    supported ? VerificationStatus.SUPPORTED : VerificationStatus.UNSUPPORTED,
                    supported,
                    confidence,
                    explanation
            );
        }
    }

    private record EvidenceSupport(
            RetrievalEvidence evidence,
            double score
    ) {
    }

    private record ComparisonClaim(
            String leftEntity,
            String rightEntity,
            int expectedComparison
    ) {
    }

    private record NumericEvidence(
            List<Double> activityValues,
            List<Double> mindWanderingValues
    ) {
        private NumericEvidence {
            activityValues = activityValues == null ? List.of() : List.copyOf(activityValues);
            mindWanderingValues = mindWanderingValues == null ? List.of() : List.copyOf(mindWanderingValues);
        }

        List<Double> valuesFor(String entity) {
            if ("mind-wandering".equals(entity)) {
                return mindWanderingValues;
            }

            return activityValues;
        }
    }
}