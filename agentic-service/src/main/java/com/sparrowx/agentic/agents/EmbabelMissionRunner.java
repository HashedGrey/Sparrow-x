package com.sparrowx.agentic.agents;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.sparrowx.agentic.mission.model.MissionResult;

import java.util.Objects;

/**
 * Focused programmatic entrypoint into the Embabel platform.
 */
public final class EmbabelMissionRunner {

    private final AgentPlatform agentPlatform;

    public EmbabelMissionRunner(AgentPlatform agentPlatform) {
        this.agentPlatform = Objects.requireNonNull(
                agentPlatform,
                "agentPlatform must not be null"
        );
    }

    public MissionResult run(MissionRunInput input) {
        Objects.requireNonNull(input, "input must not be null");

        MissionResult result = AgentInvocation
                .create(agentPlatform, MissionResult.class)
                .invoke(input);

        if (!input.missionId().equals(result.missionId())) {
            throw new IllegalStateException(
                    "Embabel returned a result for another mission"
            );
        }
        return result;
    }
}
