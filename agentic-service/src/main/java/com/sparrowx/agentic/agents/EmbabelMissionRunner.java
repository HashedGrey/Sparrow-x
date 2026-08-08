package com.sparrowx.agentic.agents;

import com.sparrowx.agentic.components.IntentComponent.IntentRequest;
import com.sparrowx.agentic.components.SynthesisComponent.SynthesisDraft;
import com.sparrowx.agentic.components.SynthesisComponent.SynthesisRequest;
import com.sparrowx.agentic.planning.MissionIntent;

import java.util.Objects;

public final class EmbabelMissionRunner {

    private final MissionAgent missionAgent;

    public EmbabelMissionRunner(MissionAgent missionAgent) {
        this.missionAgent = Objects.requireNonNull(
                missionAgent,
                "missionAgent must not be null");
    }

    public MissionIntent parseIntent(
            MissionContext context,
            IntentRequest request) {

        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(request, "request must not be null");

        requireTenant(context);
        return missionAgent.interpret(request);
    }

    public MissionAgent.TurnDecision runTurn(
            MissionContext context,
            MissionAgent.TurnInput input) {

        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(input, "input must not be null");

        requireTenant(context);
        return missionAgent.decide(input);
    }

    public SynthesisDraft synthesize(
            MissionContext context,
            SynthesisRequest request) {

        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(request, "request must not be null");

        requireTenant(context);
        return missionAgent.synthesize(request);
    }

    private static void requireTenant(MissionContext context) {
        if (context.tenantId().isBlank()) {
            throw new IllegalArgumentException(
                    "context.tenantId must not be blank");
        }
    }
}