package com.sparrowx.document.retrieval;

import com.sparrowx.document.data.postgres.entities.DocumentEntity;
import com.sparrowx.document.data.postgres.repositories.DocumentRepository;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PostgresDocumentMetadataLookup implements DocumentMetadataLookup {

    private final DocumentRepository documentRepository;

    public PostgresDocumentMetadataLookup(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<DocumentId, DocumentMetadata> findByTenantIdAndDocumentIds(
            TenantId tenantId,
            Set<DocumentId> documentIds
    ) {
        if (tenantId == null || documentIds == null || documentIds.isEmpty()) {
            return Map.of();
        }

        Set<String> rawDocumentIds = documentIds.stream()
                .filter(documentId -> documentId != null && documentId.value() != null && !documentId.value().isBlank())
                .map(DocumentId::value)
                .collect(Collectors.toSet());

        if (rawDocumentIds.isEmpty()) {
            return Map.of();
        }

        return documentRepository
                .findByTenantIdAndDocumentIdIn(
                        tenantId.value(),
                        rawDocumentIds
                )
                .stream()
                .filter(Objects::nonNull)
                .filter(entity -> entity.getDocumentId() != null && !entity.getDocumentId().isBlank())
                .collect(Collectors.toMap(
                        entity -> DocumentId.of(entity.getDocumentId()),
                        this::toMetadata,
                        (left, right) -> left
                ));
    }

    private DocumentMetadata toMetadata(DocumentEntity entity) {
        return new DocumentMetadata(
                DocumentId.of(entity.getDocumentId()),
                nullToEmpty(entity.getTitle()),
                nullToEmpty(entity.getFileName())
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}