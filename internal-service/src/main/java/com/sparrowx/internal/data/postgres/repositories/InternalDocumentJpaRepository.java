package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.InternalDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternalDocumentJpaRepository
        extends JpaRepository<InternalDocumentEntity, String> {

    Optional<InternalDocumentEntity> findByTenantIdAndDocumentId(
            String tenantId,
            String documentId
    );

    Optional<InternalDocumentEntity> findByTenantIdAndSlug(
            String tenantId,
            String slug
    );

    Optional<InternalDocumentEntity> findByTenantIdAndExternalRef(
            String tenantId,
            String externalRef
    );

    List<InternalDocumentEntity> findByTenantIdAndModuleId(
            String tenantId,
            String moduleId
    );

    List<InternalDocumentEntity> findByTenantIdAndRepositoryId(
            String tenantId,
            String repositoryId
    );

    boolean existsByTenantIdAndSlug(
            String tenantId,
            String slug
    );

    @Query("""
            select document
            from InternalDocumentEntity document
            where document.tenantId = :tenantId
              and (
                    lower(document.title) like lower(concat('%', :query, '%'))
                 or lower(document.slug) like lower(concat('%', :query, '%'))
                 or lower(document.summary) like lower(concat('%', :query, '%'))
                 or lower(document.externalRef) like lower(concat('%', :query, '%'))
              )
            """)
    List<InternalDocumentEntity> searchByTenantIdAndText(
            @Param("tenantId") String tenantId,
            @Param("query") String query
    );
}