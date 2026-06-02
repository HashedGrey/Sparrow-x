package com.sparrowx.document.ingestion.extraction;

import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;

public record ExtractedDocumentText(
        DocumentId documentId,
        ObjectKey objectKey,
        FileName fileName,
        MimeType mimeType,
        String text,
        int pageCount
) {

    public boolean isBlank() {
        return text == null || text.isBlank();
    }
}