package com.sparrowx.agentic.components;

import com.sparrowx.agentic.components.PlanningComponent.Observation;
import com.sparrowx.agentic.governance.model.GovernanceDecision;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.mission.model.Finding;
import com.sparrowx.agentic.mission.model.Recommendation;
import com.sparrowx.agentic.mission.model.ResultSection;
import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SynthesisComponent {

    private final Synthesizer synthesizer;

    public SynthesisComponent(Synthesizer synthesizer) {
        this.synthesizer = Objects.requireNonNull(
                synthesizer,
                "synthesizer must not be null");
    }

    public SynthesisDraft synthesize(SynthesisRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        if (!request.reactorComplete()) {
            throw new IllegalStateException(
                    "synthesis is allowed only after the reactor completes");
        }

        return Objects.requireNonNull(
                synthesizer.synthesize(request),
                "synthesizer returned null");
    }

    @FunctionalInterface
    public interface Synthesizer {
        SynthesisDraft synthesize(SynthesisRequest request);
    }

    public record SynthesisRequest(
            String missionId,
            boolean reactorComplete,
            MissionIntent intent,
            MissionPlan finalPlan,
            List<Observation> observations,
            List<EvidenceRef> evidenceRefs,
            List<GovernanceDecision> governanceDecisions,
            List<String> requiredSections,
            Map<String, Object> context) {

        public SynthesisRequest {
            missionId = requireText(missionId, "missionId");

            intent = Objects.requireNonNull(
                    intent,
                    "intent must not be null");

            finalPlan = Objects.requireNonNull(
                    finalPlan,
                    "finalPlan must not be null");

            observations = observations == null
                    ? List.of()
                    : List.copyOf(observations);

            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);

            governanceDecisions =
                    governanceDecisions == null
                            ? List.of()
                            : List.copyOf(governanceDecisions);

            requiredSections = requiredSections == null
                    ? List.of()
                    : List.copyOf(requiredSections);

            context = context == null
                    ? Map.of()
                    : Map.copyOf(context);
        }
    }

    public record SynthesisDraft(
            String executiveSummary,
            String finalAnswer,
            List<ResultSection> sections,
            List<Finding> findings,
            List<Recommendation> recommendations,
            Map<String, Object> structuredOutput,
            Map<String, Object> debugSummary) {

        public SynthesisDraft {
            executiveSummary = executiveSummary == null
                    ? ""
                    : executiveSummary;

            finalAnswer = requireText(
                    finalAnswer,
                    "finalAnswer");

            sections = sections == null
                    ? List.of()
                    : List.copyOf(sections);

            findings = findings == null
                    ? List.of()
                    : List.copyOf(findings);

            recommendations = recommendations == null
                    ? List.of()
                    : List.copyOf(recommendations);

            structuredOutput = structuredOutput == null
                    ? Map.of()
                    : Map.copyOf(structuredOutput);

            debugSummary = debugSummary == null
                    ? Map.of()
                    : Map.copyOf(debugSummary);
        }
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