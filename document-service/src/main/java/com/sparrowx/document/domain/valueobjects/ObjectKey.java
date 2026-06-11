package com.sparrowx.document.domain.valueobjects;

public record ObjectKey(String value) {

    public ObjectKey {
        requireText(value, "objectKey");

        if (value.contains("..")) {
            throw new IllegalArgumentException("objectKey must not contain path traversal");
        }
    }

    public static ObjectKey of(String value) {
        return new ObjectKey(value);
    }

    public static ObjectKey forDocument(
            TenantId tenantId,
            DocumentId documentId,
            FileName fileName
    ) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }

        if (documentId == null) {
            throw new IllegalArgumentException("documentId must not be null");
        }

        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null");
        }

        return new ObjectKey(
                "tenants/%s/documents/%s/%s"
                        .formatted(
                                tenantId.value(),
                                documentId.value(),
                                fileName.value()
                        )
        );
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}