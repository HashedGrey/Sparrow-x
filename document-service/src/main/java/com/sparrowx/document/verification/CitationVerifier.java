package com.sparrowx.document.verification;

import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.valueobjects.ClaimText;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.exceptions.CitationVerificationException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CitationVerifier {

    private static final double SUPPORTED_TOKEN_SCORE = 0.65;
    private static final double PARTIAL_TOKEN_SCORE = 0.35;

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "that", "this", "with", "from", "into",
            "then", "they", "their", "there", "were", "was", "are",
            "has", "had", "have", "been", "being", "which", "where"
    );

    private final NumericComparisonEvaluator numericComparisonEvaluator;

    public CitationVerifier(
            NumericComparisonEvaluator numericComparisonEvaluator
    ) {
        this.numericComparisonEvaluator = numericComparisonEvaluator;
    }

    public CitationVerificationResult verify(
            ClaimText claim,
            List<RetrievalEvidence> evidence
    ) {
        validate(claim, evidence);

        try {
            String claimText = claim.value();

            List<RetrievalEvidence> usableEvidence = evidence.stream()
                    .filter(item -> item != null)
                    .filter(item ->
                            item.text() != null
                                    && !item.text().isBlank()
                    )
                    .toList();

            if (usableEvidence.isEmpty()) {
                return new CitationVerificationResult(
                        VerificationStatus.NEEDS_SOURCE_CONTEXT,
                        false,
                        0.0,
                        "No usable evidence text was supplied for citation verification."
                );
            }

            Optional<NumericComparisonEvaluator.Result> comparisonResult =
                    numericComparisonEvaluator.evaluate(
                            claimText,
                            usableEvidence.stream()
                                    .map(RetrievalEvidence::text)
                                    .toList()
                    );

            if (comparisonResult.isPresent()) {
                return fromNumericComparison(
                        comparisonResult.get()
                );
            }

            EvidenceSupport bestSupport =
                    bestTokenOverlapSupport(
                            claimText,
                            usableEvidence
                    );

            if (bestSupport.score() >= SUPPORTED_TOKEN_SCORE) {
                return new CitationVerificationResult(
                        VerificationStatus.SUPPORTED,
                        true,
                        bestSupport.score(),
                        "Evidence node is supported by its cited source span(s)."
                );
            }

            if (bestSupport.score() >= PARTIAL_TOKEN_SCORE) {
                return new CitationVerificationResult(
                        VerificationStatus.PARTIALLY_SUPPORTED,
                        true,
                        bestSupport.score(),
                        "Evidence node is partially supported by its cited source span(s)."
                );
            }

            return new CitationVerificationResult(
                    VerificationStatus.UNSUPPORTED,
                    false,
                    bestSupport.score(),
                    "The cited source spans do not sufficiently support the evidence node."
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

    private CitationVerificationResult fromNumericComparison(
            NumericComparisonEvaluator.Result comparison
    ) {
        VerificationStatus status = comparison.status();

        boolean supported =
                status == VerificationStatus.SUPPORTED
                        || status == VerificationStatus.PARTIALLY_SUPPORTED;

        return new CitationVerificationResult(
                status,
                supported,
                comparison.confidence(),
                comparison.explanation()
        );
    }

    private EvidenceSupport bestTokenOverlapSupport(
            String claimText,
            List<RetrievalEvidence> evidence
    ) {
        Set<String> claimTokens = tokenize(claimText);

        return evidence.stream()
                .map(item ->
                        scoreEvidence(
                                claimTokens,
                                item
                        )
                )
                .max(
                        Comparator.comparingDouble(
                                EvidenceSupport::score
                        )
                )
                .orElse(
                        new EvidenceSupport(
                                null,
                                0.0
                        )
                );
    }

    private EvidenceSupport scoreEvidence(
            Set<String> claimTokens,
            RetrievalEvidence evidence
    ) {
        Set<String> evidenceTokens =
                tokenize(evidence.text());

        if (claimTokens.isEmpty()
                || evidenceTokens.isEmpty()) {
            return new EvidenceSupport(
                    evidence,
                    0.0
            );
        }

        long matches = claimTokens.stream()
                .filter(evidenceTokens::contains)
                .count();

        double score =
                (double) matches / claimTokens.size();

        return new EvidenceSupport(
                evidence,
                score
        );
    }

    private Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Stream.of(
                        normalize(value)
                                .split("[^a-z0-9]+")
                )
                .filter(token -> token.length() >= 3)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(
                        Collectors.toCollection(
                                HashSet::new
                        )
                );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void validate(
            ClaimText claim,
            List<RetrievalEvidence> evidence
    ) {
        if (claim == null) {
            throw InvalidDocumentException.blankField(
                    "claim"
            );
        }

        if (evidence == null || evidence.isEmpty()) {
            throw new CitationVerificationException(
                    "evidence must not be empty"
            );
        }
    }

    public record CitationVerificationResult(
            VerificationStatus status,
            boolean supported,
            double confidence,
            String explanation
    ) {

        public CitationVerificationResult {
            status = status == null
                    ? VerificationStatus.UNVERIFIED
                    : status;

            explanation = explanation == null
                    ? ""
                    : explanation;
        }

        public CitationVerificationResult(
                boolean supported,
                double confidence,
                String explanation
        ) {
            this(
                    supported
                            ? VerificationStatus.SUPPORTED
                            : VerificationStatus.UNSUPPORTED,
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
}