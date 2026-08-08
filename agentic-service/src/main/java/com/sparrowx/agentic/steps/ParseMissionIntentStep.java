package com.sparrowx.agentic.steps;

import com.sparrowx.agentic.agents.EmbabelMissionRunner;
import com.sparrowx.agentic.components.IntentComponent.IntentRequest;
import com.sparrowx.agentic.mission.model.MissionContext;
import com.sparrowx.agentic.planning.MissionIntent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class ParseMissionIntentStep {

    private final EmbabelMissionRunner missionRunner;

    public ParseMissionIntentStep(EmbabelMissionRunner missionRunner) {
        this.missionRunner = Objects.requireNonNull(
                missionRunner,
                "missionRunner must not be null"
        );
    }

    public MissionIntent execute(
            MissionContext context,
            IntentRequest request
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(request, "request must not be null");

        return missionRunner.parseIntent(context, request);
    }
}