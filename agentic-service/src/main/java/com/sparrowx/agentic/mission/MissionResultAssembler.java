package com.sparrowx.agentic.mission;

import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.model.MissionResult;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSerializer;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSnapshot;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MissionResultAssembler {

    private final CheckpointSerializer checkpointSerializer;

    public MissionResultAssembler(
            CheckpointSerializer checkpointSerializer
    ) {
        this.checkpointSerializer = Objects.requireNonNull(
                checkpointSerializer,
                "checkpointSerializer"
        );
    }

    public MissionResult assemble(
            Mission mission,
            CheckpointSnapshot snapshot
    ) {
        Objects.requireNonNull(mission, "mission");
        Objects.requireNonNull(snapshot, "snapshot");

        CheckpointRef reference = snapshot.reference();

        if (reference.checkpointType()
                != CheckpointRef.CheckpointType.MISSION_RESULT) {
            throw new IllegalArgumentException(
                    "Checkpoint is not a mission-result snapshot"
            );
        }

        if (!reference.tenantId().equals(mission.tenantId())
                || !reference.missionId().equals(mission.missionId())) {
            throw new IllegalArgumentException(
                    "Checkpoint does not belong to the requested mission"
            );
        }

        MissionResult result = checkpointSerializer.deserialize(
                snapshot,
                MissionResult.class
        );

        if (!result.missionId().equals(mission.missionId())) {
            throw new IllegalStateException(
                    "Mission result contains a different mission id"
            );
        }

        return result;
    }
}