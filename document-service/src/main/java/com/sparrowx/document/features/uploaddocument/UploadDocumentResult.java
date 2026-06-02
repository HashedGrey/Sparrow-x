package com.sparrowx.document.features.uploaddocument;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.DocumentStatus;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;

public record UploadDocumentResult(
        DocumentId documentId,
        IngestionJobId ingestionJobId,
        DocumentStatus status
) {
}