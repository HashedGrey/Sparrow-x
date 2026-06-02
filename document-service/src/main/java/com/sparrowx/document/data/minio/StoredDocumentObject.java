package com.sparrowx.document.data.minio;

import com.sparrowx.document.domain.valueobjects.ContentHash;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.TenantId;

import java.time.Instant;

public record StoredDocumentObject(
        TenantId tenantId,
        DocumentId documentId,
        ObjectKey objectKey,
        FileName fileName,
        MimeType mimeType,
        long sizeBytes,
        ContentHash contentHash,
        String storageProvider,
        Instant storedAt
) {
}