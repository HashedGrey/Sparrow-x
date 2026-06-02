package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.ForbiddenException;

public class DocumentAccessDeniedException extends ForbiddenException {

    public DocumentAccessDeniedException(
            String documentId,
            String userId
    ) {
        super("Access denied to document: documentId=%s userId=%s"
                .formatted(documentId, userId));
    }

    public DocumentAccessDeniedException(String message) {
        super(message);
    }
}