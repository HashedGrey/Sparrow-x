package com.sparrowx.document.verification;

import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.valueobjects.ClaimText;
import com.sparrowx.document.exceptions.CitationVerificationException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CitationVerifier {

    public CitationVerificationResult verify(
            ClaimText claim,
            List<RetrievalEvidence> evidence
    ) {
        validate(claim, evidence);

        try {
            Set<String> claimTokens = tokenize(claim.value());

            EvidenceSupport bestSupport = evidence.stream()
                    .filter(item -> item != null && item.text() != null && !item.text().isBlank())
                    .map(item -> scoreEvidence(claimTokens, item))
                    .max(Comparator.comparingDouble(EvidenceSupport::score))
                    .orElse(new EvidenceSupport(null, 0.0));

            boolean supported = bestSupport.score() >= 0.35;

            String explanation = supported
                    ? "The claim is supported by the supplied evidence with token overlap."
                    : "The supplied evidence does not provide enough support for the claim.";

            return new CitationVerificationResult(
                    supported,
                    bestSupport.score(),
                    explanation
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

        double score = (double) matches / claimTokens.size();

        return new EvidenceSupport(evidence, score);
    }

    private Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Stream.of(value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 3)
                .collect(Collectors.toSet());
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
            boolean supported,
            double confidence,
            String explanation
    ) {
    }

    private record EvidenceSupport(
            RetrievalEvidence evidence,
            double score
    ) {
    }
}