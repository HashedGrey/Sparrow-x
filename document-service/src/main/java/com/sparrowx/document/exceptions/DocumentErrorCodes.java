package com.sparrowx.document.exceptions;

public final class DocumentErrorCodes {

    private DocumentErrorCodes() {
    }

    public static final String DOCUMENT_NOT_FOUND = "DOCUMENT_NOT_FOUND";
    public static final String DOCUMENT_ACCESS_DENIED = "DOCUMENT_ACCESS_DENIED";
    public static final String INVALID_DOCUMENT = "INVALID_DOCUMENT";
    public static final String UNSUPPORTED_DOCUMENT_TYPE = "UNSUPPORTED_DOCUMENT_TYPE";

    public static final String DOCUMENT_STORAGE_FAILED = "DOCUMENT_STORAGE_FAILED";
    public static final String DOCUMENT_EXTRACTION_FAILED = "DOCUMENT_EXTRACTION_FAILED";
    public static final String DOCUMENT_CHUNKING_FAILED = "DOCUMENT_CHUNKING_FAILED";
    public static final String DOCUMENT_INDEXING_FAILED = "DOCUMENT_INDEXING_FAILED";

    public static final String INGESTION_JOB_NOT_FOUND = "INGESTION_JOB_NOT_FOUND";
    public static final String INGESTION_FAILED = "INGESTION_FAILED";

    public static final String RETRIEVAL_FAILED = "RETRIEVAL_FAILED";
    public static final String CITATION_VERIFICATION_FAILED = "CITATION_VERIFICATION_FAILED";
}