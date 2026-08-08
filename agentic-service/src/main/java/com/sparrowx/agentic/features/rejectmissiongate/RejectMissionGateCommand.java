package com.sparrowx.agentic.features.rejectmissiongate;

import buildingblocks.core.commands.Command;

public record RejectMissionGateCommand(
        MissionContext context,
        String missionId,
        String gateId,
        String reason
) implements Command<RejectMissionGateResult> {

    public RejectMissionGateCommand {
        missionId = nullToEmpty(missionId);
        gateId = nullToEmpty(gateId);
        reason = nullToEmpty(reason);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}