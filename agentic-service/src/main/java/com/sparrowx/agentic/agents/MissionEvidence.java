package com.sparrowx.agentic.agents;

import com.sparrowx.agentic.components.PlanningComponent.Observation;
import com.sparrowx.agentic.mission.evidence.Citation;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record MissionEvidence(
        List<Observation> observations,
        List<EvidenceRef> evidenceRefs,
        List<Citation> citations,
        Set<String> verifiedEvidenceIds,
        List<String> warnings,
        Map<String, String> excerptsByEvidenceId
) {

    public MissionEvidence {
        observations = observations == null ? List.of() : List.copyOf(observations);
        evidenceRefs = evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
        citations = citations == null ? List.of() : List.copyOf(citations);
        verifiedEvidenceIds = verifiedEvidenceIds == null ? Set.of() : Set.copyOf(verifiedEvidenceIds);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        excerptsByEvidenceId = excerptsByEvidenceId == null ? Map.of() : Map.copyOf(excerptsByEvidenceId);
    }
}