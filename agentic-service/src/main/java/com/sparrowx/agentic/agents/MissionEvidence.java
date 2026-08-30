package com.sparrowx.agentic.agents;

import com.sparrowx.agentic.components.PlanningComponent.Observation;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;

import java.util.List;
import java.util.Map;

/**
 * Evidence and observations produced by SparrowX enterprise capabilities.
 * This is ordinary Embabel blackboard data; Temporal never reads it.
 */
public record MissionEvidence(
        List<Observation> observations,
        List<EvidenceRef> evidenceRefs,
        List<String> warnings,
        Map<String, String> excerptsByEvidenceId
) {

    public MissionEvidence {
        observations = observations == null
                ? List.of()
                : List.copyOf(observations);
        evidenceRefs = evidenceRefs == null
                ? List.of()
                : List.copyOf(evidenceRefs);
        warnings = warnings == null
                ? List.of()
                : List.copyOf(warnings);
        excerptsByEvidenceId = excerptsByEvidenceId == null
                ? Map.of()
                : Map.copyOf(excerptsByEvidenceId);
    }
}
