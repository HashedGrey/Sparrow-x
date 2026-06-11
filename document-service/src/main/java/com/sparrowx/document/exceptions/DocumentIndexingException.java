package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.InternalServerException;

public class DocumentIndexingException extends InternalServerException {

    public DocumentIndexingException(String message) {
        super(message);
    }

    public DocumentIndexingException(
            String message,
            Exception cause
    ) {
        super(message, cause);
    }

    public DocumentIndexingException(
            String message,
            Throwable cause
    ) {
        super(message, asException(cause));
    }

    private static Exception asException(Throwable cause) {
        if (cause instanceof Exception exception) {
            return exception;
        }

        return new RuntimeException(cause);
    }
}