package com.sparrowx.document.data.postgres.repositories;

import com.sparrowx.document.data.postgres.entities.DocumentChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunkEntity, String> {

    Optional<DocumentChunkEntity> findByChunkId(String chunkId);

    List<DocumentChunkEntity> findByDocumentId(String documentId);

    List<DocumentChunkEntity> findByDocumentIdOrderByChunkIndexAsc(String documentId);

    List<DocumentChunkEntity> findByTenantIdAndDocumentIdOrderByChunkIndexAsc(
            String tenantId,
            String documentId
    );

    void deleteByDocumentId(String documentId);
}