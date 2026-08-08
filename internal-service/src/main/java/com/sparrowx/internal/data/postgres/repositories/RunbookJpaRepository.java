package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.RunbookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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

    @Query("""
            select runbook
            from RunbookEntity runbook
            where runbook.tenantId = :tenantId
              and (
                    lower(runbook.title) like lower(concat('%', :query, '%'))
                 or lower(runbook.slug) like lower(concat('%', :query, '%'))
                 or lower(runbook.summary) like lower(concat('%', :query, '%'))
              )
            """)
    List<RunbookEntity> searchByTenantIdAndText(
            @Param("tenantId") String tenantId,
            @Param("query") String query
    );
}