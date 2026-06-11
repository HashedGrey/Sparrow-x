package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.RunbookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RunbookJpaRepository extends JpaRepository<RunbookEntity, String> {

    Optional<RunbookEntity> findByTenantIdAndRunbookId(
            String tenantId,
            String runbookId
    );

    Optional<RunbookEntity> findByTenantIdAndSlug(
            String tenantId,
            String slug
    );

    List<RunbookEntity> findByTenantIdAndModuleId(
            String tenantId,
            String moduleId
    );

    List<RunbookEntity> findByTenantIdAndDocumentId(
            String tenantId,
            String documentId
    );

    boolean existsByTenantIdAndSlug(
            String tenantId,
            String slug
    );
}