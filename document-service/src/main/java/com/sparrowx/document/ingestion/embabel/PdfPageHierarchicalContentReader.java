package com.sparrowx.document.ingestion.embabel;

import com.embabel.agent.rag.model.DefaultMaterializedContainerSection;
import com.embabel.agent.rag.model.LeafSection;
import com.embabel.agent.rag.model.MaterializedDocument;
import com.embabel.agent.rag.model.NavigableDocument;
import com.embabel.agent.rag.model.NavigableSection;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.exceptions.DocumentExtractionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PdfPageHierarchicalContentReader {

    private static final String PDF_MIME_TYPE = "application/pdf";

    public boolean supports(MimeType mimeType) {
        return mimeType != null
                && PDF_MIME_TYPE.equals(mimeType.value());
    }

    public NavigableDocument parse(
            DocumentId documentId,
            ObjectKey objectKey,
            FileName fileName,
            byte[] content
    ) {
        try (PDDocument pdf = Loader.loadPDF(content)) {

            String rootId = UUID.randomUUID().toString();
            int pageCount = pdf.getNumberOfPages();

            Map<String, Object> rootMetadata = new LinkedHashMap<>();
            rootMetadata.put("Page-Count", pageCount);
            rootMetadata.put("sparrowx_document_id", documentId.value());
            rootMetadata.put("sparrowx_object_key", objectKey.value());
            rootMetadata.put("sparrowx_file_name", fileName.value());
            rootMetadata.put("sparrowx_mime_type", PDF_MIME_TYPE);
            rootMetadata.put("root_document_id", rootId);

            String title = resolveTitle(pdf, fileName);

            List<NavigableSection> pageSections = new ArrayList<>();

            for (int pageNumber = 1;
                 pageNumber <= pageCount;
                 pageNumber++) {

                String pageText = extractPage(
                        pdf,
                        pageNumber
                );

                /*
                 * Do not manufacture retrievable content for an empty page.
                 * The document-level page count still remains correct.
                 */
                if (pageText.isBlank()) {
                    continue;
                }

                String pageSectionId =
                        UUID.randomUUID().toString();

                String leafId =
                        UUID.randomUUID().toString();

                String pageUri =
                        objectKey.value()
                                + "#page="
                                + pageNumber;

                Map<String, Object> pageMetadata =
                        pageMetadata(rootId, pageNumber);

                LeafSection leaf = new LeafSection(
                        leafId,
                        pageUri,
                        "Page " + pageNumber,
                        pageText,
                        pageSectionId,
                        pageMetadata
                );

                List<NavigableSection> children =
                        List.of(leaf);

                DefaultMaterializedContainerSection pageSection =
                        new DefaultMaterializedContainerSection(
                                pageSectionId,
                                pageUri,
                                "Page " + pageNumber,
                                children,
                                rootId,
                                pageMetadata
                        );

                pageSections.add(pageSection);
            }

            return new MaterializedDocument(
                    rootId,
                    objectKey.value(),
                    title,
                    Instant.now(),
                    pageSections,
                    rootMetadata
            );

        } catch (Exception exception) {
            throw new DocumentExtractionException(
                    "Failed to parse PDF into page-aware Embabel content: "
                            + fileName.value(),
                    exception
            );
        }
    }

    private String extractPage(
            PDDocument pdf,
            int pageNumber
    ) throws Exception {

        PDFTextStripper stripper =
                new PDFTextStripper();

        stripper.setStartPage(pageNumber);
        stripper.setEndPage(pageNumber);

        String text = stripper.getText(pdf);

        return text == null
                ? ""
                : text.trim();
    }

    private Map<String, Object> pageMetadata(
            String rootId,
            int pageNumber
    ) {
        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put("root_document_id", rootId);
        metadata.put("page_number", pageNumber);
        metadata.put("page_start", pageNumber);
        metadata.put("page_end", pageNumber);

        return metadata;
    }

    private String resolveTitle(
            PDDocument pdf,
            FileName fileName
    ) {
        String title =
                pdf.getDocumentInformation() == null
                        ? null
                        : pdf.getDocumentInformation().getTitle();

        if (title != null && !title.isBlank()) {
            return title.trim();
        }

        return fileName.value();
    }
}