package com.sparrowx.internal.data.postgres.repositories;

import com.sparrowx.internal.data.postgres.entities.InternalDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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
}