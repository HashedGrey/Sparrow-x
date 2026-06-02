package com.sparrowx.document.data.minio;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.TenantId;

public interface DocumentStorage {

    StoredDocumentObject store(StoreDocumentObjectRequest request);

    byte[] read(String objectKey);

    boolean exists(String objectKey);

    void delete(String objectKey);

    record StoreDocumentObjectRequest(
            TenantId tenantId,
            DocumentId documentId,
            ObjectKey objectKey,
            FileName fileName,
            MimeType mimeType,
            byte[] content
    ) {
    }
}