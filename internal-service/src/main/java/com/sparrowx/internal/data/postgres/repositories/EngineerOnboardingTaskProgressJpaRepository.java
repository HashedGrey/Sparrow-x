package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.EngineerOnboardingTaskProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EngineerOnboardingTaskProgressJpaRepository
        extends JpaRepository<EngineerOnboardingTaskProgressEntity, String> {

    Optional<EngineerOnboardingTaskProgressEntity> findByTenantIdAndTaskProgressId(
            String tenantId,
            String taskProgressId
    );

    Optional<EngineerOnboardingTaskProgressEntity> findByTenantIdAndAssignmentIdAndOnboardingTaskId(
            String tenantId,
            String assignmentId,
            String onboardingTaskId
    );

    List<EngineerOnboardingTaskProgressEntity> findByTenantIdAndAssignmentId(
            String tenantId,
            String assignmentId
    );

    boolean existsByTenantIdAndAssignmentIdAndOnboardingTaskId(
            String tenantId,
            String assignmentId,
            String onboardingTaskId
    );
}