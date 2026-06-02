package com.sparrowx.document.ingestion.chunking;

import com.sparrowx.document.ingestion.extraction.ExtractedDocumentText;

import java.util.List;

public interface DocumentChunker {

    List<DocumentChunkDraft> chunk(ExtractedDocumentText extractedText);
}