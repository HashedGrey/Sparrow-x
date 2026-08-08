package com.sparrowx.agentic.mission;

import com.sparrowx.agentic.exceptions.MissionNotFoundException;
import com.sparrowx.agentic.features.getmissionresult.GetMissionResultView;
import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.model.MissionFailure;
import com.sparrowx.agentic.mission.model.MissionResult;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.mission.store.MissionStore;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointStore;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MissionResultReadService {

    private final MissionStore missionStore;
    private final CheckpointStore checkpointStore;
    private final MissionResultAssembler resultAssembler;

    public MissionResultReadService(
            MissionStore missionStore,
            CheckpointStore checkpointStore,
            MissionResultAssembler resultAssembler
    ) {
        this.missionStore = Objects.requireNonNull(
                missionStore,
                "missionStore"
        );
        this.checkpointStore = Objects.requireNonNull(
                checkpointStore,
                "checkpointStore"
        );
        this.resultAssembler = Objects.requireNonNull(
                resultAssembler,
                "resultAssembler"
        );
    }

    public GetMissionResultView read(
            String tenantId,
            String missionId
    ) {
        Mission mission = missionStore.findById(tenantId, missionId)
                .orElseThrow(() -> new MissionNotFoundException(
                        tenantId,
                        missionId
                ));

        MissionResult result = null;
        MissionFailure error = null;

        if (mission.status() == MissionStatus.COMPLETED) {
            result = checkpointStore.findLatest(
                            tenantId,
                            missionId,
                            CheckpointRef.CheckpointType.MISSION_RESULT
                    )
                    .map(snapshot -> resultAssembler.assemble(
                            mission,
                            snapshot
                    ))
                    .orElseThrow(() -> new IllegalStateException(
                            "Completed mission has no result checkpoint: "
                                    + missionId
                    ));
        }

        if (mission.status() == MissionStatus.FAILED_RETRYABLE
                || mission.status() == MissionStatus.FAILED_TERMINAL) {
            error = Objects.requireNonNull(
                    mission.failure(),
                    "Failed mission has no failure details"
            );
        }

        return new GetMissionResultView(
                mission.missionId(),
                mission.status(),
                result,
                error,
                mission.status() == MissionStatus.WAITING_APPROVAL
                        ? mission.waitState()
                        : null,
                mission.completedAt()
        );
    }
}