package com.sparrowx.internal.exceptions;

public class InternalServiceException extends RuntimeException {

    private final InternalErrorCodes errorCode;

    public InternalServiceException(String message) {
        super(message);
        this.errorCode = InternalErrorCodes.INTERNAL_SERVICE_ERROR;
    }

    public InternalServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = InternalErrorCodes.INTERNAL_SERVICE_ERROR;
    }

    public InternalServiceException(
            InternalErrorCodes errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }

    public InternalServiceException(
            InternalErrorCodes errorCode,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public InternalErrorCodes getErrorCode() {
        return errorCode;
    }
}