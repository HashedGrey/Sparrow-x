package com.sparrowx.internal.exceptions;

public class InternalValidationException extends InternalServiceException {

    public InternalValidationException(String message) {
        super(InternalErrorCodes.VALIDATION_FAILED, message);
    }

    public InternalValidationException(String message, Throwable cause) {
        super(InternalErrorCodes.VALIDATION_FAILED, message, cause);
    }
}