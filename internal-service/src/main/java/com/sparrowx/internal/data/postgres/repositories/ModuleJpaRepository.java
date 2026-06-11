package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuleJpaRepository extends JpaRepository<ModuleEntity, String> {

    Optional<ModuleEntity> findByTenantIdAndModuleId(
            String tenantId,
            String moduleId
    );

    Optional<ModuleEntity> findByTenantIdAndSlug(
            String tenantId,
            String slug
    );

    List<ModuleEntity> findByTenantIdAndOwningTeamId(
            String tenantId,
            String owningTeamId
    );

    boolean existsByTenantIdAndSlug(
            String tenantId,
            String slug
    );
}