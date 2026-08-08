package com.sparrowx.agentic.validation;

import com.sparrowx.agentic.governance.GroundingPolicy;
import com.sparrowx.agentic.mission.evidence.Citation;
import com.sparrowx.agentic.mission.evidence.EvidenceRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
public final class GroundingValidator {

    public GroundingPolicy.Assessment validate(
            List<GroundingPolicy.ClaimEvidence> claims,
            EvidenceRegistry evidenceRegistry,
            List<Citation> citations,
            Set<String> verifiedEvidenceIds,
            GroundingPolicy.Requirements requirements
    ) {
        if (evidenceRegistry == null) {
            throw new IllegalArgumentException(
                    "GROUNDING_EVIDENCE_REGISTRY_REQUIRED"
            );
        }

        if (requirements == null) {
            throw new IllegalArgumentException(
                    "GROUNDING_REQUIREMENTS_REQUIRED"
            );
        }

        List<GroundingPolicy.ClaimEvidence> safeClaims =
                claims == null
                        ? List.of()
                        : Collections.unmodifiableList(
                        new ArrayList<>(claims)
                );

        List<Citation> safeCitations = citations == null
                ? List.of()
                : Collections.unmodifiableList(
                new ArrayList<>(citations)
        );

        Set<String> safeVerifiedIds =
                normalizedIds(verifiedEvidenceIds);

        List<String> violations = new ArrayList<>();
        Set<String> claimIds = new HashSet<>();

        for (GroundingPolicy.ClaimEvidence claim : safeClaims) {
            if (claim == null) {
                violations.add("NULL_CLAIM");
                continue;
            }

            if (!claimIds.add(claim.claimId())) {
                violations.add(
                        "DUPLICATE_CLAIM_ID:" + claim.claimId()
                );
            }
        }

        Set<String> citedEvidenceIds = validateCitations(
                safeCitations,
                evidenceRegistry,
                violations
        );

        for (String verifiedId : safeVerifiedIds) {
            if (evidenceRegistry.findById(verifiedId).isEmpty()) {
                violations.add(
                        "UNKNOWN_VERIFIED_EVIDENCE:" + verifiedId
                );
            }
        }

        int materialClaims = 0;
        int groundedClaims = 0;
        int citedClaims = 0;
        double minimumConfidence =
                safeClaims.isEmpty() ? 0.0d : 1.0d;

        Set<String> missingEvidenceIds = new TreeSet<>();
        Set<String> usedEvidenceIds = new TreeSet<>();

        for (GroundingPolicy.ClaimEvidence claim : safeClaims) {
            if (claim == null) {
                continue;
            }

            if (claim.material()) {
                materialClaims++;
            }

            minimumConfidence = Math.min(
                    minimumConfidence,
                    claim.confidence()
            );

            int registeredLinks = 0;
            boolean hasCitation = false;

            for (String evidenceId : claim.evidenceIds()) {
                if (evidenceRegistry.findById(evidenceId).isPresent()) {
                    registeredLinks++;
                    usedEvidenceIds.add(evidenceId);
                    hasCitation |= citedEvidenceIds.contains(evidenceId);
                } else {
                    missingEvidenceIds.add(evidenceId);
                }
            }

            int requiredLinks = claim.material()
                    ? requirements.minimumEvidencePerMaterialClaim()
                    : 1;

            if (registeredLinks >= requiredLinks) {
                groundedClaims++;
            }

            if (hasCitation) {
                citedClaims++;
            }
        }

        int claimCount = safeClaims.size();
        double claimCoverage = ratio(
                groundedClaims,
                claimCount
        );
        double citationCoverage = ratio(
                citedClaims,
                claimCount
        );

        boolean verificationPassed = !usedEvidenceIds.isEmpty()
                && safeVerifiedIds.containsAll(usedEvidenceIds);

        if (requirements.requireEvidence() && claimCount == 0) {
            violations.add("NO_CLAIMS");
        }

        if (!missingEvidenceIds.isEmpty()) {
            violations.add(
                    "MISSING_EVIDENCE:"
                            + String.join(",", missingEvidenceIds)
            );
        }

        if (requirements.requireEvidence()
                && claimCoverage
                < requirements.minimumClaimCoverage()) {
            violations.add("INSUFFICIENT_CLAIM_COVERAGE");
        }

        if (requirements.requireCitations()
                && citationCoverage
                < requirements.minimumCitationCoverage()) {
            violations.add("INSUFFICIENT_CITATION_COVERAGE");
        }

        if (minimumConfidence < requirements.minimumConfidence()) {
            violations.add("LOW_CONFIDENCE");
        }

        if (requirements.requireVerification()
                && !verificationPassed) {
            violations.add("VERIFICATION_REQUIRED");
        }

        GroundingPolicy.Assessment assessment =
                new GroundingPolicy.Assessment(
                        claimCount,
                        materialClaims,
                        groundedClaims,
                        citedClaims,
                        evidenceRegistry.size(),
                        (int) usedEvidenceIds.stream()
                                .filter(safeVerifiedIds::contains)
                                .count(),
                        claimCoverage,
                        citationCoverage,
                        minimumConfidence,
                        missingEvidenceIds,
                        verificationPassed
                );

        if (!violations.isEmpty()) {
            throw violation(violations);
        }

        return assessment;
    }

    private static Set<String> validateCitations(
            List<Citation> citations,
            EvidenceRegistry evidenceRegistry,
            List<String> violations
    ) {
        Set<String> citationIds = new HashSet<>();
        Set<String> evidenceIds = new HashSet<>();

        for (Citation citation : citations) {
            if (citation == null) {
                violations.add("NULL_CITATION");
                continue;
            }

            if (citation.citationId() == null
                    || citation.citationId().isBlank()) {
                violations.add("CITATION_ID_REQUIRED");
            } else if (!citationIds.add(citation.citationId())) {
                violations.add(
                        "DUPLICATE_CITATION_ID:"
                                + citation.citationId()
                );
            }

            String evidenceId = citation.evidenceId();
            if (evidenceId == null || evidenceId.isBlank()) {
                violations.add("CITATION_EVIDENCE_ID_REQUIRED");
                continue;
            }

            if (evidenceRegistry.findById(evidenceId).isEmpty()) {
                violations.add(
                        "CITATION_WITHOUT_EVIDENCE:" + evidenceId
                );
                continue;
            }

            if (!evidenceIds.add(evidenceId)) {
                violations.add(
                        "DUPLICATE_CITATION_EVIDENCE:" + evidenceId
                );
            }
        }

        return evidenceIds;
    }

    private static Set<String> normalizedIds(
            Set<String> values
    ) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        TreeSet<String> normalized = new TreeSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(
                        "GROUNDING_VERIFIED_EVIDENCE_ID_INVALID"
                );
            }
            normalized.add(value.trim());
        }

        return Collections.unmodifiableSet(normalized);
    }

    private static double ratio(
            int numerator,
            int denominator
    ) {
        return denominator == 0
                ? 0.0d
                : (double) numerator / denominator;
    }

    private static IllegalArgumentException violation(
            List<String> violations
    ) {
        return new IllegalArgumentException(
                "GROUNDING_VALIDATION_FAILED: "
                        + String.join("|", violations)
        );
    }
}