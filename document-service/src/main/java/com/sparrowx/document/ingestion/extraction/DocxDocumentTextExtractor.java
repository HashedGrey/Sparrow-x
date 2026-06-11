package com.sparrowx.document.ingestion.extraction;

import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.exceptions.DocumentExtractionException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.exceptions.UnsupportedDocumentTypeException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class DocxDocumentTextExtractor {

    private static final String DOCX_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    public ExtractedDocumentText extract(DocumentTextExtractor.ExtractTextRequest request) {
        validate(request);

        try (InputStream inputStream = new ByteArrayInputStream(request.content())) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            metadata.set(Metadata.CONTENT_TYPE, request.mimeType().value());
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, request.fileName().value());
            new OOXMLParser().parse(inputStream, handler, metadata, parseContext);

            String text = handler.toString();

            return new ExtractedDocumentText(
                    request.documentId(),
                    request.objectKey(),
                    request.fileName(),
                    request.mimeType(),
                    text == null ? "" : text.trim(),
                    1
            );
        } catch (Exception exception) {
            throw new DocumentExtractionException(
                    "Failed to extract DOCX text from document: " + request.fileName().value(),
                    exception
            );
        }
    }

    public boolean supports(MimeType mimeType) {
        return mimeType != null && DOCX_MIME_TYPE.equals(mimeType.value());
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