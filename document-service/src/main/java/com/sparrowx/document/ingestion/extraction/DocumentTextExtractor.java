package com.sparrowx.document.ingestion.extraction;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;

public interface DocumentTextExtractor {

    ExtractedDocumentText extract(ExtractTextRequest request);

    record ExtractTextRequest(
            DocumentId documentId,
            ObjectKey objectKey,
            FileName fileName,
            MimeType mimeType,
            byte[] content
    ) {
    }
}