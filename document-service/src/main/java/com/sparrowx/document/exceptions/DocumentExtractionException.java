package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.InternalServerException;

public class DocumentExtractionException extends InternalServerException {

    public DocumentExtractionException(String message) {
        super(message);
    }

    public DocumentExtractionException(
            String message,
            Exception cause
    ) {
        super(message, cause);
    }

    public DocumentExtractionException(
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