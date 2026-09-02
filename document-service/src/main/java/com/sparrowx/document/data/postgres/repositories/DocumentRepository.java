package com.sparrowx.document.data.postgres.repositories;

import com.sparrowx.document.data.postgres.entities.DocumentEntity;
import com.sparrowx.document.domain.valueobjects.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    Optional<DocumentEntity> findByDocumentId(String documentId);

    Optional<DocumentEntity> findByDocumentIdAndTenantId(
            String documentId,
            String tenantId
    );

    Optional<DocumentEntity> findByTenantIdAndContentHash(
            String tenantId,
            String contentHash
    );

    List<DocumentEntity> findByTenantId(String tenantId);

    List<DocumentEntity> findByTenantIdAndStatus(
            String tenantId,
            DocumentStatus status
    );

    List<DocumentEntity> findByTenantIdAndDocumentIdIn(
            String tenantId,
            Collection<String> documentIds
    );

    List<DocumentEntity> findByTenantIdAndStatusAndFileName(
            String tenantId,
            DocumentStatus status,
            String fileName
    );

    List<DocumentEntity> findByTenantIdAndStatusAndTitle(
            String tenantId,
            DocumentStatus status,
            String title
    );
}