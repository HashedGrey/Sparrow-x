package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.OnboardingTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("""
            select onboardingTask
            from OnboardingTaskEntity onboardingTask
            where onboardingTask.tenantId = :tenantId
              and (
                    lower(onboardingTask.title) like lower(concat('%', :query, '%'))
                 or lower(onboardingTask.description) like lower(concat('%', :query, '%'))
              )
            """)
    List<OnboardingTaskEntity> searchByTenantIdAndText(
            @Param("tenantId") String tenantId,
            @Param("query") String query
    );
}