package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.InternalServerException;

public class RetrievalFailedException extends InternalServerException {

    public RetrievalFailedException(String message) {
        super(message);
    }

    public RetrievalFailedException(
            String message,
            Exception cause
    ) {
        super(message, cause);
    }

    public RetrievalFailedException(
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