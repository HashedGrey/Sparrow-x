package com.sparrowx.agentic.governance;

import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.governance.model.GovernanceDecisionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Component
public final class GroundingPolicy {

    public GovernanceDecision evaluate(
            String decisionId,
            Requirements requirements,
            Assessment assessment
    ) {
        requireText(decisionId, "decisionId");
        Objects.requireNonNull(
                requirements,
                "requirements must not be null"
        );
        Objects.requireNonNull(
                assessment,
                "assessment must not be null"
        );

        List<String> failures = new ArrayList<>();

        if (requirements.requireEvidence()
                && assessment.claimCount() == 0) {
            failures.add("NO_CLAIMS");
        }

        if (requirements.requireEvidence()
                && assessment.claimCoverage()
                < requirements.minimumClaimCoverage()) {
            failures.add("INSUFFICIENT_CLAIM_COVERAGE");
        }

        if (!assessment.missingEvidenceIds().isEmpty()) {
            failures.add("MISSING_EVIDENCE");
        }

        if (requirements.requireCitations()
                && assessment.citationCoverage()
                < requirements.minimumCitationCoverage()) {
            failures.add("INSUFFICIENT_CITATION_COVERAGE");
        }

        if (assessment.minimumConfidence()
                < requirements.minimumConfidence()) {
            failures.add("LOW_CONFIDENCE");
        }

        if (requirements.requireVerification()
                && !assessment.verificationPassed()) {
            failures.add("VERIFICATION_REQUIRED");
        }

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("failureCodes", List.copyOf(failures));
        attributes.put("claimCount", assessment.claimCount());
        attributes.put(
                "materialClaimCount",
                assessment.materialClaimCount()
        );
        attributes.put(
                "groundedClaimCount",
                assessment.groundedClaimCount()
        );
        attributes.put(
                "citedClaimCount",
                assessment.citedClaimCount()
        );
        attributes.put(
                "registeredEvidenceCount",
                assessment.registeredEvidenceCount()
        );
        attributes.put(
                "verifiedEvidenceCount",
                assessment.verifiedEvidenceCount()
        );
        attributes.put(
                "claimCoverage",
                assessment.claimCoverage()
        );
        attributes.put(
                "citationCoverage",
                assessment.citationCoverage()
        );
        attributes.put(
                "minimumConfidence",
                assessment.minimumConfidence()
        );
        attributes.put(
                "missingEvidenceCount",
                assessment.missingEvidenceIds().size()
        );
        attributes.put(
                "verificationPassed",
                assessment.verificationPassed()
        );

        return new GovernanceDecision(
                decisionId,
                "grounding",
                failures.isEmpty()
                        ? GovernanceDecisionType.ALLOWED
                        : GovernanceDecisionType.DENIED,
                failures.isEmpty()
                        ? "Claims satisfy evidence, citation, confidence and verification policy."
                        : "Grounding policy failed: "
                        + String.join(",", failures),
                attributes
        );
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }

    private static double requireRatio(
            double value,
            String field
    ) {
        if (!Double.isFinite(value)
                || value < 0.0d
                || value > 1.0d) {
            throw new IllegalArgumentException(
                    field + " must be between 0 and 1"
            );
        }

        return value;
    }

    public record Requirements(
            boolean requireEvidence,
            boolean requireCitations,
            boolean requireVerification,
            double minimumClaimCoverage,
            double minimumCitationCoverage,
            double minimumConfidence,
            int minimumEvidencePerMaterialClaim
    ) {
        public Requirements {
            minimumClaimCoverage = requireRatio(
                    minimumClaimCoverage,
                    "minimumClaimCoverage"
            );
            minimumCitationCoverage = requireRatio(
                    minimumCitationCoverage,
                    "minimumCitationCoverage"
            );
            minimumConfidence = requireRatio(
                    minimumConfidence,
                    "minimumConfidence"
            );

            if (minimumEvidencePerMaterialClaim < 1) {
                throw new IllegalArgumentException(
                        "minimumEvidencePerMaterialClaim must be >= 1"
                );
            }
        }
    }

    public record ClaimEvidence(
            String claimId,
            List<String> evidenceIds,
            double confidence,
            boolean material
    ) {
        public ClaimEvidence {
            claimId = requireText(claimId, "claimId");
            confidence = requireRatio(
                    confidence,
                    "confidence"
            );

            evidenceIds = evidenceIds == null
                    ? List.of()
                    : evidenceIds.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .distinct()
                    .sorted()
                    .toList();
        }
    }

    public record Assessment(
            int claimCount,
            int materialClaimCount,
            int groundedClaimCount,
            int citedClaimCount,
            int registeredEvidenceCount,
            int verifiedEvidenceCount,
            double claimCoverage,
            double citationCoverage,
            double minimumConfidence,
            Set<String> missingEvidenceIds,
            boolean verificationPassed
    ) {
        public Assessment {
            if (claimCount < 0
                    || materialClaimCount < 0
                    || groundedClaimCount < 0
                    || citedClaimCount < 0
                    || registeredEvidenceCount < 0
                    || verifiedEvidenceCount < 0) {
                throw new IllegalArgumentException(
                        "assessment counts must be >= 0"
                );
            }

            if (materialClaimCount > claimCount
                    || groundedClaimCount > claimCount
                    || citedClaimCount > claimCount
                    || verifiedEvidenceCount
                    > registeredEvidenceCount) {
                throw new IllegalArgumentException(
                        "assessment counts are inconsistent"
                );
            }

            claimCoverage = requireRatio(
                    claimCoverage,
                    "claimCoverage"
            );
            citationCoverage = requireRatio(
                    citationCoverage,
                    "citationCoverage"
            );
            minimumConfidence = requireRatio(
                    minimumConfidence,
                    "minimumConfidence"
            );

            TreeSet<String> normalizedMissing =
                    new TreeSet<>();

            if (missingEvidenceIds != null) {
                missingEvidenceIds.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .forEach(normalizedMissing::add);
            }

            missingEvidenceIds =
                    Collections.unmodifiableSet(normalizedMissing);
        }
    }
}