package com.sparrowx.internal.exceptions;

public class InternalPermissionDeniedException extends InternalServiceException {

    public InternalPermissionDeniedException(String message) {
        super(InternalErrorCodes.PERMISSION_DENIED, message);
    }

    public InternalPermissionDeniedException(String message, Throwable cause) {
        super(InternalErrorCodes.PERMISSION_DENIED, message, cause);
    }
}