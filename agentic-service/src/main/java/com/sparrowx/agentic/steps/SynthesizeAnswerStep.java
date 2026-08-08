package com.sparrowx.agentic.steps;

import com.sparrowx.agentic.actions.synthesis.BuildCitationsAction;
import com.sparrowx.agentic.agents.EmbabelMissionRunner;
import com.sparrowx.agentic.components.SynthesisComponent.SynthesisDraft;
import com.sparrowx.agentic.components.SynthesisComponent.SynthesisRequest;
import com.sparrowx.agentic.mission.model.MissionResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public final class SynthesizeAnswerStep {

    private final EmbabelMissionRunner missionRunner;
    private final BuildCitationsAction buildCitationsAction;

    public SynthesizeAnswerStep(
            EmbabelMissionRunner missionRunner,
            BuildCitationsAction buildCitationsAction
    ) {
        this.missionRunner = Objects.requireNonNull(
                missionRunner,
                "missionRunner must not be null"
        );
        this.buildCitationsAction = Objects.requireNonNull(
                buildCitationsAction,
                "buildCitationsAction must not be null"
        );
    }

    public MissionResult execute(
            MissionContext context,
            SynthesisRequest request,
            Map<String, String> excerptsByEvidenceId
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(request, "request must not be null");

        if (!request.reactorComplete()) {
            throw new IllegalStateException(
                    "synthesis requires a completed reactor"
            );
        }

        SynthesisDraft draft =
                missionRunner.synthesize(context, request);

        BuildCitationsAction.Result citations =
                buildCitationsAction.execute(
                        new BuildCitationsAction.BuildSpec(
                                request.evidenceRefs(),
                                excerptsByEvidenceId
                        )
                );

        return new MissionResult(
                request.missionId(),
                draft.executiveSummary(),
                draft.finalAnswer(),
                draft.sections(),
                draft.findings(),
                draft.recommendations(),
                citations.evidenceRefs(),
                citations.citations(),
                request.governanceDecisions(),
                draft.structuredOutput(),
                draft.debugSummary()
        );
    }
}