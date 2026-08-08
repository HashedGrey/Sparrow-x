package com.sparrowx.agentic.features.getmissionresult;

import com.sparrowx.agentic.mission.model.HumanGateState;
import com.sparrowx.agentic.mission.model.MissionFailure;
import com.sparrowx.agentic.mission.model.MissionResult;
import com.sparrowx.agentic.mission.model.MissionStatus;

import java.time.Instant;
import java.util.Objects;

public record GetMissionResultView(
        String missionId,
        MissionStatus status,
        MissionResult result,
        MissionFailure error,
        HumanGateState waitState,
        Instant completedAt
) {

    public GetMissionResultView {
        missionId = requireText(missionId, "missionId");
        status = Objects.requireNonNull(
                status,
                "status must not be null"
        );

        int payloadCount = 0;
        payloadCount += result == null ? 0 : 1;
        payloadCount += error == null ? 0 : 1;
        payloadCount += waitState == null ? 0 : 1;

        if (payloadCount > 1) {
            throw new IllegalArgumentException(
                    "Only one of result, error, or waitState may be present"
            );
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }
}