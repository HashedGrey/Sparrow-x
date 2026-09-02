package com.sparrowx.document.retrieval;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public final class DocumentScopeResolver {

    private final DocumentMetadataLookup documentMetadataLookup;

    public DocumentScopeResolver(
            DocumentMetadataLookup documentMetadataLookup
    ) {
        this.documentMetadataLookup = documentMetadataLookup;
    }

    public Set<DocumentId> resolve(
            TenantId tenantId,
            List<DocumentId> requestedDocumentIds,
            List<String> requestedFileNames
    ) {
        if (tenantId == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        Set<DocumentId> resolved = new LinkedHashSet<>();

        resolveDocumentIds(
                tenantId,
                requestedDocumentIds,
                resolved
        );

        resolveFileNames(
                tenantId,
                requestedFileNames,
                resolved
        );

        return Set.copyOf(resolved);
    }

    private void resolveDocumentIds(
            TenantId tenantId,
            List<DocumentId> requestedDocumentIds,
            Set<DocumentId> resolved
    ) {
        if (requestedDocumentIds == null || requestedDocumentIds.isEmpty()) {
            return;
        }

        Set<DocumentId> normalized = new LinkedHashSet<>();

        for (DocumentId documentId : requestedDocumentIds) {
            if (documentId == null
                    || documentId.value() == null
                    || documentId.value().isBlank()) {
                continue;
            }

            normalized.add(documentId);
        }

        if (normalized.isEmpty()) {
            return;
        }

        Map<DocumentId, DocumentMetadataLookup.DocumentMetadata> matches =
                documentMetadataLookup.findByTenantIdAndDocumentIds(
                        tenantId,
                        normalized
                );

        for (DocumentId requested : normalized) {
            DocumentMetadataLookup.DocumentMetadata metadata =
                    matches.get(requested);

            if (metadata == null || metadata.documentId() == null) {
                throw InvalidDocumentException.scopeTargetNotFound(
                        "document_id",
                        requested.value()
                );
            }

            resolved.add(metadata.documentId());
        }
    }

    private void resolveFileNames(
            TenantId tenantId,
            List<String> requestedFileNames,
            Set<DocumentId> resolved
    ) {
        if (requestedFileNames == null || requestedFileNames.isEmpty()) {
            return;
        }

        Set<String> normalized = new LinkedHashSet<>();

        for (String value : requestedFileNames) {
            if (value != null && !value.isBlank()) {
                normalized.add(value.trim());
            }
        }

        for (String value : normalized) {
            resolved.add(
                    resolveFileNameOrTitle(
                            tenantId,
                            value
                    )
            );
        }
    }

    private DocumentId resolveFileNameOrTitle(
            TenantId tenantId,
            String value
    ) {
        List<DocumentMetadataLookup.DocumentMetadata> fileNameMatches =
                documentMetadataLookup.findReadyByTenantIdAndFileName(
                        tenantId,
                        value
                );

        if (fileNameMatches.size() == 1) {
            return fileNameMatches.getFirst().documentId();
        }

        if (fileNameMatches.size() > 1) {
            throw InvalidDocumentException.ambiguousScopeTarget(
                    "file_name",
                    value,
                    fileNameMatches.size()
            );
        }

        List<DocumentMetadataLookup.DocumentMetadata> titleMatches =
                documentMetadataLookup.findReadyByTenantIdAndTitle(
                        tenantId,
                        value
                );

        if (titleMatches.size() == 1) {
            return titleMatches.getFirst().documentId();
        }

        if (titleMatches.size() > 1) {
            throw InvalidDocumentException.ambiguousScopeTarget(
                    "title",
                    value,
                    titleMatches.size()
            );
        }

        throw InvalidDocumentException.scopeTargetNotFound(
                "file_name_or_title",
                value
        );
    }
}