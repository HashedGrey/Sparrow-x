package com.sparrowx.agentic.data.postgres.repositories;

import com.sparrowx.agentic.data.postgres.entities.HumanGateEntity;
import com.sparrowx.agentic.runtime.gate.HumanGateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HumanGateJpaRepository
        extends JpaRepository<HumanGateEntity, Long> {

    Optional<HumanGateEntity> findByTenantIdAndMissionIdAndGateId(
            String tenantId,
            String missionId,
            String gateId
    );

    Optional<HumanGateEntity>
    findFirstByTenantIdAndMissionIdAndStatusOrderByCreatedAtDescGateIdDesc(
            String tenantId,
            String missionId,
            HumanGateStatus status
    );
}