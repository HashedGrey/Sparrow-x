package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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

    @Query("""
            select repository
            from RepositoryEntity repository
            where repository.tenantId = :tenantId
              and (
                    lower(repository.name) like lower(concat('%', :query, '%'))
                 or lower(repository.url) like lower(concat('%', :query, '%'))
              )
            """)
    List<RepositoryEntity> searchByTenantIdAndText(
            @Param("tenantId") String tenantId,
            @Param("query") String query
    );
}