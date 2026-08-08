package com.sparrowx.agentic.features.approvemissiongate;

import buildingblocks.core.commands.Command;

import java.util.Objects;

public record ApproveMissionGateCommand(
        MissionContext context,
        String missionId,
        String gateId,
        String note
) implements Command<ApproveMissionGateResult> {

    public ApproveMissionGateCommand {
        context = Objects.requireNonNull(
                context,
                "context must not be null"
        );
        missionId = normalize(missionId);
        gateId = normalize(gateId);
        note = normalize(note);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}