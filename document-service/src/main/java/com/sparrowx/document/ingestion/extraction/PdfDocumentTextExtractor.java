package com.sparrowx.document.ingestion.extraction;

import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.exceptions.DocumentExtractionException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.exceptions.UnsupportedDocumentTypeException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class PdfDocumentTextExtractor {

    private static final String PDF_MIME_TYPE = "application/pdf";

    public ExtractedDocumentText extract(DocumentTextExtractor.ExtractTextRequest request) {
        validate(request);

        try (InputStream inputStream = new ByteArrayInputStream(request.content())) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            metadata.set(Metadata.CONTENT_TYPE, request.mimeType().value());
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, request.fileName().value());
            new PDFParser().parse(inputStream, handler, metadata, parseContext);

            String text = handler.toString();

            return new ExtractedDocumentText(
                    request.documentId(),
                    request.objectKey(),
                    request.fileName(),
                    request.mimeType(),
                    text == null ? "" : text.trim(),
                    extractPageCount(metadata)
            );

        } catch (Exception exception) {
            throw new DocumentExtractionException(
                    "Failed to extract PDF text from document: " + request.fileName().value(),
                    exception
            );
        }
    }

    public boolean supports(MimeType mimeType) {
        return mimeType != null && PDF_MIME_TYPE.equals(mimeType.value());
    }

    private int extractPageCount(Metadata metadata) {
        String[] candidates = {
                "xmpTPg:NPages",
                "Page-Count",
                "meta:page-count"
        };

        for (String candidate : candidates) {
            String value = metadata.get(candidate);

            if (value == null || value.isBlank()) {
                continue;
            }

            try {
                return Math.max(1, Integer.parseInt(value.trim()));
            } catch (NumberFormatException ignored) {
                // Try next key.
            }
        }

        return 1;
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
            throw InvalidDocumentException.emptyContent();
        }

        if (!supports(request.mimeType())) {
            throw new UnsupportedDocumentTypeException(request.mimeType().value());
        }
    }
}