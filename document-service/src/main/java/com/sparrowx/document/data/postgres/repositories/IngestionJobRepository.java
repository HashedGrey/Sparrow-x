package com.sparrowx.document.data.postgres.repositories;

import com.sparrowx.document.data.postgres.entities.IngestionJobEntity;
import com.sparrowx.document.domain.valueobjects.IngestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngestionJobRepository extends JpaRepository<IngestionJobEntity, String> {

    Optional<IngestionJobEntity> findByIngestionJobId(String ingestionJobId);

    List<IngestionJobEntity> findByStatus(IngestionStatus status);

    List<IngestionJobEntity> findByStatusIn(List<IngestionStatus> statuses);

    List<IngestionJobEntity> findByDocumentId(String documentId);

    Optional<IngestionJobEntity> findByIngestionJobIdAndTenantId(
            String ingestionJobId,
            String tenantId
    );
}