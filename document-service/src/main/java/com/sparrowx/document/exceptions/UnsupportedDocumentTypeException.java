package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.BadRequestException;

import java.util.List;

public class UnsupportedDocumentTypeException extends BadRequestException {

    public UnsupportedDocumentTypeException(String mimeType) {
        super(
                "Unsupported document type: " + mimeType,
                List.of("Unsupported document type: " + mimeType)
        );
    }
}