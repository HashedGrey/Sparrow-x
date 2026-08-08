package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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

    @Query("""
            select module
            from ModuleEntity module
            where module.tenantId = :tenantId
              and (
                    lower(module.name) like lower(concat('%', :query, '%'))
                 or lower(module.slug) like lower(concat('%', :query, '%'))
                 or lower(module.description) like lower(concat('%', :query, '%'))
              )
            """)
    List<ModuleEntity> searchByTenantIdAndText(
            @Param("tenantId") String tenantId,
            @Param("query") String query
    );
}