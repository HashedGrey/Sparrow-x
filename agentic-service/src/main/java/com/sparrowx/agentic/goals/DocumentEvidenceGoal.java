package com.sparrowx.agentic.goals;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record DocumentEvidenceGoal(
        String missionId,
        Set<String> documentIds,
        List<String> evidenceObjectives,
        boolean verificationRequired,
        int minimumEvidenceItems,
        Map<String, Object> attributes) {

    public DocumentEvidenceGoal {
        missionId = requireText(missionId, "missionId");
        documentIds = documentIds == null
                ? Set.of()
                : Set.copyOf(documentIds);
        evidenceObjectives = evidenceObjectives == null
                ? List.of()
                : List.copyOf(evidenceObjectives);

        if (minimumEvidenceItems <= 0) {
            throw new IllegalArgumentException(
                    "minimumEvidenceItems must be positive");
        }

        attributes = attributes == null
                ? Map.of()
                : Map.copyOf(attributes);
    }

    private static String requireText(
            String value,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }

        return value;
    }
}