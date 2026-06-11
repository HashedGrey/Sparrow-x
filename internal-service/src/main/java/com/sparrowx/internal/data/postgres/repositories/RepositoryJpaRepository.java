package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositoryJpaRepository extends JpaRepository<RepositoryEntity, String> {

    Optional<RepositoryEntity> findByTenantIdAndRepositoryId(
            String tenantId,
            String repositoryId
    );

    Optional<RepositoryEntity> findByTenantIdAndUrl(
            String tenantId,
            String url
    );

    List<RepositoryEntity> findByTenantIdAndModuleId(
            String tenantId,
            String moduleId
    );

    boolean existsByTenantIdAndUrl(
            String tenantId,
            String url
    );
}