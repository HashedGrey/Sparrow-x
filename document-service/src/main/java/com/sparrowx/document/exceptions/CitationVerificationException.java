package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.InternalServerException;

public class CitationVerificationException extends InternalServerException {

    public CitationVerificationException(String message) {
        super(message);
    }

    public CitationVerificationException(
            String message,
            Exception cause
    ) {
        super(message, cause);
    }

    public CitationVerificationException(
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