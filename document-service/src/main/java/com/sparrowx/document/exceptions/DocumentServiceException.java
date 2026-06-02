package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class DocumentServiceException extends AppException {

    private static final int DOCUMENT_SERVICE_ERROR_CODE = 5000;

    public DocumentServiceException(String message) {
        super(
                message,
                HttpStatus.BAD_REQUEST,
                DOCUMENT_SERVICE_ERROR_CODE
        );
    }

    public DocumentServiceException(
            String message,
            HttpStatus status
    ) {
        super(
                message,
                status,
                DOCUMENT_SERVICE_ERROR_CODE
        );
    }

    public DocumentServiceException(
            String message,
            Exception cause
    ) {
        super(
                message,
                cause,
                DOCUMENT_SERVICE_ERROR_CODE
        );
    }

    public DocumentServiceException(
            String message,
            Throwable cause
    ) {
        super(
                message,
                asException(cause),
                DOCUMENT_SERVICE_ERROR_CODE
        );
    }

    private static Exception asException(Throwable cause) {
        if (cause instanceof Exception exception) {
            return exception;
        }

        return new RuntimeException(cause);
    }
}