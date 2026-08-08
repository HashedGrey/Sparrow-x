package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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

    @Query("""
            select permission
            from PermissionEntity permission
            where permission.tenantId = :tenantId
              and (
                    lower(permission.name) like lower(concat('%', :query, '%'))
                 or lower(permission.description) like lower(concat('%', :query, '%'))
              )
            """)
    List<PermissionEntity> searchByTenantIdAndText(
            @Param("tenantId") String tenantId,
            @Param("query") String query
    );
}