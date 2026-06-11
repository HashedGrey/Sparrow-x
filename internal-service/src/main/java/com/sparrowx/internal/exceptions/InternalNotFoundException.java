package com.sparrowx.internal.exceptions;

public class InternalNotFoundException extends InternalServiceException {

    public InternalNotFoundException(String message) {
        super(InternalErrorCodes.NOT_FOUND, message);
    }

    public InternalNotFoundException(String message, Throwable cause) {
        super(InternalErrorCodes.NOT_FOUND, message, cause);
    }
}