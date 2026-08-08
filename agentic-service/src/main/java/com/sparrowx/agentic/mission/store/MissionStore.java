package com.sparrowx.agentic.mission.store;

import com.sparrowx.agentic.mission.model.Mission;

import java.util.Optional;

/**
 * Sole persistence authority for the business Mission aggregate.
 */
public interface MissionStore {

    /**
     * Atomically creates the candidate or returns the mission already associated
     * with the same tenant and request id.
     */
    Mission createOrGet(Mission candidate);

    Mission save(Mission mission);

    Optional<Mission> findById(String tenantId, String missionId);

    Optional<Mission> findByRequestId(String tenantId, String requestId);
}