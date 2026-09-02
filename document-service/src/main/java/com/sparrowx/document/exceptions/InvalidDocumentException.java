package com.sparrowx.document.exceptions;

import buildingblocks.shared.exceptions.BadRequestException;

import java.util.List;

public class InvalidDocumentException extends BadRequestException {

    public InvalidDocumentException(String message) {
        super(message);
    }

    public InvalidDocumentException(String message, List<String> errors) {
        super(message, errors);
    }

    public static InvalidDocumentException blankField(String fieldName) {
        return new InvalidDocumentException(
                fieldName + " must not be blank",
                List.of(fieldName + " must not be blank")
        );
    }

    public static InvalidDocumentException emptyContent() {
        return new InvalidDocumentException(
                "content must not be empty",
                List.of("content must not be empty")
        );
    }

    public static InvalidDocumentException nullCommand(String commandName) {
        return new InvalidDocumentException(
                commandName + " must not be null",
                List.of(commandName + " must not be null")
        );
    }

    public static InvalidDocumentException nullQuery(String queryName) {
        return new InvalidDocumentException(
                queryName + " must not be null",
                List.of(queryName + " must not be null")
        );
    }

    public static InvalidDocumentException scopeTargetNotFound(
            String selectorType,
            String value
    ) {
        String error = selectorType + " did not resolve to a document: " + value;

        return new InvalidDocumentException(
                "Document scope could not be resolved",
                List.of(error)
        );
    }

    public static InvalidDocumentException ambiguousScopeTarget(
            String selectorType,
            String value,
            int matchCount
    ) {
        String error = selectorType
                + " resolved to multiple documents: "
                + value
                + " (matches="
                + matchCount
                + ")";

        return new InvalidDocumentException(
                "Document scope is ambiguous",
                List.of(error)
        );
    }

    public static InvalidDocumentException unsupportedScopeOnly() {
        return new InvalidDocumentException(
                "Document scope could not be safely resolved",
                List.of(
                        "collection_ids, tags, and metadata_filters are not yet enforced; "
                                + "provide document_ids or file_names"
                )
        );
    }
}