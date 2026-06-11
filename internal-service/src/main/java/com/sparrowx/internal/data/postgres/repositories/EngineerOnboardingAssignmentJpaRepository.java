package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.EngineerOnboardingAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EngineerOnboardingAssignmentJpaRepository
        extends JpaRepository<EngineerOnboardingAssignmentEntity, String> {

    Optional<EngineerOnboardingAssignmentEntity> findByTenantIdAndAssignmentId(
            String tenantId,
            String assignmentId
    );

    Optional<EngineerOnboardingAssignmentEntity> findByTenantIdAndEngineerIdAndOnboardingPathId(
            String tenantId,
            String engineerId,
            String onboardingPathId
    );

    List<EngineerOnboardingAssignmentEntity> findByTenantIdAndEngineerId(
            String tenantId,
            String engineerId
    );

    List<EngineerOnboardingAssignmentEntity> findByTenantIdAndOnboardingPathId(
            String tenantId,
            String onboardingPathId
    );

    boolean existsByTenantIdAndEngineerIdAndOnboardingPathId(
            String tenantId,
            String engineerId,
            String onboardingPathId
    );
}