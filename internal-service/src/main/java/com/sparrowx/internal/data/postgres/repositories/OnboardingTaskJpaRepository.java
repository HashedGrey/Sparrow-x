package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.OnboardingTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingTaskJpaRepository
        extends JpaRepository<OnboardingTaskEntity, String> {

    Optional<OnboardingTaskEntity> findByTenantIdAndOnboardingTaskId(
            String tenantId,
            String onboardingTaskId
    );

    List<OnboardingTaskEntity> findByTenantIdAndOnboardingPathIdOrderBySortOrderAsc(
            String tenantId,
            String onboardingPathId
    );

    List<OnboardingTaskEntity> findByTenantIdAndDocumentId(
            String tenantId,
            String documentId
    );

    List<OnboardingTaskEntity> findByTenantIdAndRunbookId(
            String tenantId,
            String runbookId
    );
}