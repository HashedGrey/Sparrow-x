package com.sparrowx.agentic.components;

import com.embabel.agent.api.common.OperationContext;
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

/**
 * Produces the final grounded mission answer using the Embabel AI runtime
 * associated with the current action execution.
 *
 * The LLM-facing SynthesisProjection is intentionally small and permissive.
 * Strict SparrowX domain output is constructed only after the projection
 * has been returned by the model.
 */
public final class SynthesisComponent {

    public SynthesisDraft synthesize(
            SynthesisRequest request,
            OperationContext context
    ) {
        Objects.requireNonNull(
                request,
                "request must not be null"
        );
        Objects.requireNonNull(
                context,
                "context must not be null"
        );

        if (!request.reactorComplete()) {
            throw new IllegalStateException(
                    "synthesis is allowed only after the reactor completes"
            );
        }

        SynthesisProjection projection =
                context.ai()
                        .withDefaultLlm()
                        .createObject(
                                synthesisPrompt(request),
                                SynthesisProjection.class
                        );

        return toDraft(projection);
    }

    /**
     * Builds the model input from the frozen mission reasoning state.
     *
     * No retrieval or tool execution should happen here. By this point,
     * evidence collection has completed and synthesis operates only over
     * the supplied intent, plan, observations and evidence references.
     */
    private static String synthesisPrompt(
            SynthesisRequest request
    ) {
        return """
                You are the final synthesis stage of SparrowX.

                Produce a grounded final response for the mission below.

                <mission_id>
                %s
                </mission_id>

                <mission_intent>
                %s
                </mission_intent>

                <final_plan>
                %s
                </final_plan>

                <observations>
                %s
                </observations>

                <evidence_references>
                %s
                </evidence_references>

                <governance_decisions>
                %s
                </governance_decisions>

                <required_sections>
                %s
                </required_sections>

                <mission_context>
                %s
                </mission_context>

                Requirements:

                - Answer the user's original objective directly.
                - Use only the supplied mission state, observations and evidence.
                - Do not invent facts, entities, relationships or evidence.
                - Preserve uncertainty when the evidence is incomplete.
                - Respect warnings and governance decisions present in the context.
                - Do not claim that evidence proves something it does not support.
                - The executiveSummary should briefly state the important result.
                - The finalAnswer should contain the complete user-facing answer.
                - Do not include implementation/debug commentary in the finalAnswer.
                """.formatted(
                request.missionId(),
                request.intent(),
                request.finalPlan(),
                request.observations(),
                request.evidenceRefs(),
                request.governanceDecisions(),
                request.requiredSections(),
                request.context()
        );
    }

    /**
     * Converts the loose LLM-facing projection into SparrowX's strict
     * synthesis domain result.
     *
     * Keep complex domain types out of the LLM projection until the basic
     * end-to-end synthesis path has been validated.
     */
    private static SynthesisDraft toDraft(
            SynthesisProjection projection
    ) {
        Objects.requireNonNull(
                projection,
                "synthesis projection must not be null"
        );

        return new SynthesisDraft(
                projection.executiveSummary(),
                projection.finalAnswer(),
                List.of(),
                List.of(),
                List.of(),
                Map.of(),
                Map.of(
                        "synthesisEngine",
                        "embabel-operation-context"
                )
        );
    }

    /**
     * LLM-facing structured output.
     *
     * This deliberately does not expose ResultSection, Finding,
     * Recommendation or other strict domain records directly to the model.
     */
    public record SynthesisProjection(
            String executiveSummary,
            String finalAnswer
    ) {
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
            Map<String, Object> context
    ) {
        public SynthesisRequest {
            missionId = requireText(
                    missionId,
                    "missionId"
            );

            intent = Objects.requireNonNull(
                    intent,
                    "intent must not be null"
            );

            finalPlan = Objects.requireNonNull(
                    finalPlan,
                    "finalPlan must not be null"
            );

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
            Map<String, Object> debugSummary
    ) {
        public SynthesisDraft {
            executiveSummary = executiveSummary == null
                    ? ""
                    : executiveSummary;

            finalAnswer = requireText(
                    finalAnswer,
                    "finalAnswer"
            );

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
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value;
    }
}