package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, String> {

    Optional<PermissionEntity> findByTenantIdAndPermissionId(
            String tenantId,
            String permissionId
    );

    Optional<PermissionEntity> findByTenantIdAndName(
            String tenantId,
            String name
    );

    boolean existsByTenantIdAndName(
            String tenantId,
            String name
    );
}