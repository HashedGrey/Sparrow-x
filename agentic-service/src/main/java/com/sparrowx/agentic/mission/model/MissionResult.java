package com.sparrowx.agentic.mission.model;

import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.mission.evidence.Citation;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete final result returned by the public Agentic API.
 */
public record MissionResult(
        String missionId,
        String executiveSummary,
        String finalAnswer,
        List<ResultSection> sections,
        List<Finding> findings,
        List<Recommendation> recommendations,
        List<EvidenceRef> evidenceRefs,
        List<Citation> citations,
        List<GovernanceDecision> governanceDecisions,
        Map<String, Object> structuredOutput,
        Map<String, Object> debugSummary
) {

    public MissionResult {
        missionId = nullToEmpty(missionId);
        executiveSummary = nullToEmpty(executiveSummary);
        finalAnswer = nullToEmpty(finalAnswer);
        sections = sections == null ? List.of() : List.copyOf(sections);
        findings = findings == null ? List.of() : List.copyOf(findings);
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
        evidenceRefs = evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
        citations = citations == null ? List.of() : List.copyOf(citations);
        governanceDecisions = governanceDecisions == null
                ? List.of()
                : List.copyOf(governanceDecisions);
        structuredOutput = immutableStruct(structuredOutput);
        debugSummary = immutableStruct(debugSummary);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Map<String, Object> immutableStruct(Map<String, Object> value) {
        return value == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }
}