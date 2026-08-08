package com.sparrowx.agentic.runtime.store;

import com.sparrowx.agentic.runtime.gate.HumanGate;

import java.util.Optional;

/**
 * Tenant-scoped persistence port for auditable human approval gates.
 */
public interface HumanGateStore {

    HumanGate create(HumanGate gate);

    HumanGate save(HumanGate gate);

    Optional<HumanGate> findById(
            String tenantId,
            String missionId,
            String gateId
    );

    Optional<HumanGate> findOpenByMission(
            String tenantId,
            String missionId
    );
}