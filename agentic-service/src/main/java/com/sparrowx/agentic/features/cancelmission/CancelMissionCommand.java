package com.sparrowx.agentic.features.cancelmission;

import buildingblocks.core.commands.Command;
import com.sparrowx.agentic.mission.model.MissionContext;

import java.util.Objects;

public record CancelMissionCommand(
        MissionContext context,
        String missionId,
        String reason
) implements Command<CancelMissionResult> {

    public CancelMissionCommand {
        context = Objects.requireNonNull(
                context,
                "context must not be null"
        );
        missionId = normalize(missionId);
        reason = normalize(reason);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}