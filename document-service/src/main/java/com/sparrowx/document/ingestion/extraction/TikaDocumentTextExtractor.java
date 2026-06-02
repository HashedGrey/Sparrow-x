package com.sparrowx.document.ingestion.extraction;

import com.sparrowx.document.exceptions.DocumentExtractionException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.exceptions.UnsupportedDocumentTypeException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Primary
@Component
public class TikaDocumentTextExtractor implements DocumentTextExtractor {

    private final PdfDocumentTextExtractor pdfDocumentTextExtractor;
    private final DocxDocumentTextExtractor docxDocumentTextExtractor;
    private final XlsxDocumentTextExtractor xlsxDocumentTextExtractor;
    private final OcrDocumentTextExtractor ocrDocumentTextExtractor;

    public TikaDocumentTextExtractor(
            PdfDocumentTextExtractor pdfDocumentTextExtractor,
            DocxDocumentTextExtractor docxDocumentTextExtractor,
            XlsxDocumentTextExtractor xlsxDocumentTextExtractor,
            OcrDocumentTextExtractor ocrDocumentTextExtractor
    ) {
        this.pdfDocumentTextExtractor = pdfDocumentTextExtractor;
        this.docxDocumentTextExtractor = docxDocumentTextExtractor;
        this.xlsxDocumentTextExtractor = xlsxDocumentTextExtractor;
        this.ocrDocumentTextExtractor = ocrDocumentTextExtractor;
    }

    @Override
    public ExtractedDocumentText extract(ExtractTextRequest request) {
        validate(request);

        if (pdfDocumentTextExtractor.supports(request.mimeType())) {
            ExtractedDocumentText extracted = pdfDocumentTextExtractor.extract(request);

            if (!extracted.isBlank()) {
                return extracted;
            }

            if (ocrDocumentTextExtractor.supports(request.mimeType())) {
                return ocrDocumentTextExtractor.extract(request);
            }

            return extracted;
        }

        if (docxDocumentTextExtractor.supports(request.mimeType())) {
            return docxDocumentTextExtractor.extract(request);
        }

        if (xlsxDocumentTextExtractor.supports(request.mimeType())) {
            return xlsxDocumentTextExtractor.extract(request);
        }

        if (ocrDocumentTextExtractor.supports(request.mimeType())) {
            return ocrDocumentTextExtractor.extract(request);
        }

        return autoDetect(request);
    }

    private ExtractedDocumentText autoDetect(ExtractTextRequest request) {
        try (InputStream inputStream = new ByteArrayInputStream(request.content())) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            metadata.set(Metadata.CONTENT_TYPE, request.mimeType().value());
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, request.fileName().value());
            parser.parse(inputStream, handler, metadata, parseContext);

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
                    "Failed to extract text from document: " + request.fileName().value(),
                    exception
            );
        }
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
                // Try next metadata key.
            }
        }

        return 1;
    }

    private void validate(ExtractTextRequest request) {
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

        if (!isSupported(request.mimeType().value())) {
            throw new UnsupportedDocumentTypeException(request.mimeType().value());
        }
    }

    private boolean isSupported(String mimeType) {
        return switch (mimeType) {
            case "application/pdf",
                 "application/msword",
                 "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                 "application/vnd.ms-excel",
                 "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                 "text/plain",
                 "image/png",
                 "image/jpeg",
                 "image/tiff" -> true;
            default -> false;
        };
    }
}