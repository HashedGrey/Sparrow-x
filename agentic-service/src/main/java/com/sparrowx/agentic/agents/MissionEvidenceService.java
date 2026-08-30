package com.sparrowx.agentic.agents;

import com.sparrowx.agentic.planning.MissionIntent;
import com.sparrowx.agentic.planning.MissionPlan;

/**
 * Enterprise capability boundary used by the Embabel evidence action.
 */
public interface MissionEvidenceService {

    MissionEvidence collect(
            MissionRunInput input,
            MissionIntent intent,
            MissionPlan plan
    );
}
