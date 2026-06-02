package com.sparrowx.document.ingestion.extraction;

import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.exceptions.DocumentExtractionException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;

@Component
public class OcrDocumentTextExtractor {

    public ExtractedDocumentText extract(DocumentTextExtractor.ExtractTextRequest request) {
        validate(request);

        /*
         * Placeholder OCR extractor.
         *
         * Later replace this with:
         * - Tesseract
         * - cloud OCR
         * - PDF image extraction + OCR
         *
         * Current behavior intentionally returns empty text instead of failing
         * scanned PDFs/images. The pipeline can still complete with 0 chunks.
         */
        return new ExtractedDocumentText(
                request.documentId(),
                request.objectKey(),
                request.fileName(),
                request.mimeType(),
                "",
                1
        );
    }

    public boolean supports(MimeType mimeType) {
        return mimeType != null && (
                "image/png".equals(mimeType.value())
                        || "image/jpeg".equals(mimeType.value())
                        || "image/tiff".equals(mimeType.value())
                        || "application/pdf".equals(mimeType.value())
        );
    }

    private void validate(DocumentTextExtractor.ExtractTextRequest request) {
        if (request == null) {
            throw InvalidDocumentException.nullCommand("ExtractTextRequest");
        }

        if (request.documentId() == null) {
            throw InvalidDocumentException.blankField("documentId");
        }

        if (request.objectKey() == null) {
            throw InvalidDocumentException.blankField("objectKey");
        }

        if (request.fileName() == null) {
            throw InvalidDocumentException.blankField("fileName");
        }

        if (request.mimeType() == null) {
            throw InvalidDocumentException.blankField("mimeType");
        }

        if (request.content() == null || request.content().length == 0) {
            throw new DocumentExtractionException(
                    "Cannot OCR empty document content: " + request.fileName().value()
            );
        }
    }
}