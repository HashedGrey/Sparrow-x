package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.NotFoundException;

public class DocumentNotFoundException extends NotFoundException {

    public DocumentNotFoundException(String documentId) {
        super("Document not found: " + documentId);
    }

    public DocumentNotFoundException(
            String documentId,
            String tenantId
    ) {
        super("Document not found: documentId=%s tenantId=%s"
                .formatted(documentId, tenantId));
    }
}