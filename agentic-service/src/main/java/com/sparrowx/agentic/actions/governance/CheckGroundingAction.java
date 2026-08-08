package com.sparrowx.agentic.actions.governance;

import com.embabel.agent.api.annotation.Action;
import com.sparrowx.agentic.governance.GroundingPolicy;
import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.mission.evidence.Citation;
import com.sparrowx.agentic.mission.evidence.EvidenceRegistry;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class CheckGroundingAction {

    private final GroundingPolicy groundingPolicy;

    public CheckGroundingAction(GroundingPolicy groundingPolicy) {
        this.groundingPolicy = Objects.requireNonNull(
                groundingPolicy,
                "groundingPolicy must not be null"
        );
    }

    @Action
    public Result execute(CheckSpec spec, EvidenceRegistry evidenceRegistry) {
        Objects.requireNonNull(spec, "spec must not be null");
        Objects.requireNonNull(
                evidenceRegistry,
                "evidenceRegistry must not be null"
        );

        ensureUniqueClaimIds(spec.claims());

        Set<String> citationEvidenceIds = registeredCitationEvidenceIds(
                spec.citations(),
                evidenceRegistry
        );
        Set<String> verifiedEvidenceIds = registeredEvidenceIds(
                spec.verifiedEvidenceIds(),
                evidenceRegistry
        );

        int groundedClaims = 0;
        int citedClaims = 0;
        int materialClaims = 0;
        double minimumConfidence = spec.claims().isEmpty() ? 0.0d : 1.0d;

        Set<String> missingEvidenceIds = new TreeSet<>();
        Set<String> usedEvidenceIds = new TreeSet<>();

        for (GroundingPolicy.ClaimEvidence claim : spec.claims()) {
            if (claim.material()) {
                materialClaims++;
            }

            minimumConfidence = Math.min(
                    minimumConfidence,
                    claim.confidence()
            );

            int registeredLinks = 0;
            boolean cited = false;

            for (String evidenceId : claim.evidenceIds()) {
                if (evidenceRegistry.findById(evidenceId).isPresent()) {
                    registeredLinks++;
                    usedEvidenceIds.add(evidenceId);
                    cited |= citationEvidenceIds.contains(evidenceId);
                } else {
                    missingEvidenceIds.add(evidenceId);
                }
            }

            int requiredLinks = claim.material()
                    ? spec.requirements().minimumEvidencePerMaterialClaim()
                    : 1;

            if (registeredLinks >= requiredLinks) {
                groundedClaims++;
            }
            if (cited) {
                citedClaims++;
            }
        }

        int claimCount = spec.claims().size();
        boolean verificationPassed = !usedEvidenceIds.isEmpty()
                && verifiedEvidenceIds.containsAll(usedEvidenceIds);

        GroundingPolicy.Assessment assessment =
                new GroundingPolicy.Assessment(
                        claimCount,
                        materialClaims,
                        groundedClaims,
                        citedClaims,
                        evidenceRegistry.size(),
                        (int) usedEvidenceIds.stream()
                                .filter(verifiedEvidenceIds::contains)
                                .count(),
                        ratio(groundedClaims, claimCount),
                        ratio(citedClaims, claimCount),
                        minimumConfidence,
                        missingEvidenceIds,
                        verificationPassed
                );

        GovernanceDecision decision = groundingPolicy.evaluate(
                spec.decisionId(),
                spec.requirements(),
                assessment
        );

        return new Result(assessment, decision);
    }

    private static Set<String> registeredCitationEvidenceIds(
            List<Citation> citations,
            EvidenceRegistry registry
    ) {
        Set<String> result = new HashSet<>();

        for (Citation citation : citations) {
            if (citation != null
                    && citation.evidenceId() != null
                    && registry.findById(citation.evidenceId()).isPresent()) {
                result.add(citation.evidenceId());
            }
        }

        return result;
    }

    private static Set<String> registeredEvidenceIds(
            Set<String> candidates,
            EvidenceRegistry registry
    ) {
        Set<String> result = new HashSet<>();

        for (String evidenceId : candidates) {
            if (registry.findById(evidenceId).isPresent()) {
                result.add(evidenceId);
            }
        }

        return result;
    }

    private static void ensureUniqueClaimIds(
            List<GroundingPolicy.ClaimEvidence> claims
    ) {
        Set<String> ids = new HashSet<>();

        for (GroundingPolicy.ClaimEvidence claim : claims) {
            if (!ids.add(claim.claimId())) {
                throw new IllegalArgumentException(
                        "claimId must be unique: " + claim.claimId()
                );
            }
        }
    }

    private static double ratio(int numerator, int denominator) {
        return denominator == 0
                ? 0.0d
                : (double) numerator / denominator;
    }

    public record CheckSpec(
            String decisionId,
            List<GroundingPolicy.ClaimEvidence> claims,
            List<Citation> citations,
            Set<String> verifiedEvidenceIds,
            GroundingPolicy.Requirements requirements
    ) {
        public CheckSpec {
            decisionId = requireText(decisionId, "decisionId");
            claims = claims == null ? List.of() : List.copyOf(claims);
            citations = citations == null ? List.of() : List.copyOf(citations);
            verifiedEvidenceIds = immutableIds(verifiedEvidenceIds);
            requirements = Objects.requireNonNull(
                    requirements,
                    "requirements must not be null"
            );
        }
    }

    public record Result(
            GroundingPolicy.Assessment assessment,
            GovernanceDecision decision
    ) {
        public Result {
            assessment = Objects.requireNonNull(
                    assessment,
                    "assessment must not be null"
            );
            decision = Objects.requireNonNull(
                    decision,
                    "decision must not be null"
            );
        }
    }

    private static Set<String> immutableIds(Set<String> values) {
        TreeSet<String> normalized = new TreeSet<>();

        if (values != null) {
            values.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .forEach(normalized::add);
        }

        return Collections.unmodifiableSet(normalized);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}