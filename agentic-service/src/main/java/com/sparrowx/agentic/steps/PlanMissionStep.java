package com.sparrowx.agentic.steps;

import com.sparrowx.agentic.agents.EmbabelMissionRunner;
import com.sparrowx.agentic.agents.MissionAgent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class PlanMissionStep {

    private final EmbabelMissionRunner missionRunner;

    public PlanMissionStep(EmbabelMissionRunner missionRunner) {
        this.missionRunner = Objects.requireNonNull(
                missionRunner,
                "missionRunner must not be null"
        );
    }

    public MissionAgent.TurnDecision execute(
            MissionContext context,
            MissionAgent.TurnInput input
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(input, "input must not be null");

        return missionRunner.runTurn(context, input);
    }
}