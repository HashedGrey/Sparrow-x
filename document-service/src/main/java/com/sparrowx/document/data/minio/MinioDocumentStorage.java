package com.sparrowx.document.data.minio;

import com.sparrowx.document.config.MinioConfig;
import com.sparrowx.document.domain.valueobjects.ContentHash;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.exceptions.DocumentServiceException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Map;

@Component
@ConditionalOnBean(MinioClient.class)
public class MinioDocumentStorage implements DocumentStorage {

    private static final String STORAGE_PROVIDER = "minio";

    private static final String META_CONTENT_HASH = "content-hash";
    private static final String META_FILE_NAME = "file-name";
    private static final String META_MIME_TYPE = "mime-type";
    private static final String META_STORED_AT = "stored-at";
    private static final String META_TENANT_ID = "tenant-id";
    private static final String META_DOCUMENT_ID = "document-id";

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public MinioDocumentStorage(
            MinioClient minioClient,
            MinioConfig minioConfig
    ) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    @Override
    public StoredDocumentObject store(StoreDocumentObjectRequest request) {
        validate(request);

        try {
            ensureBucketExists();

            byte[] content = request.content();
            long sizeBytes = content.length;
            Instant storedAt = Instant.now();
            ContentHash contentHash = ContentHash.sha256(content);

            Map<String, String> metadata = Map.of(
                    META_CONTENT_HASH, contentHash.value(),
                    META_FILE_NAME, request.fileName().value(),
                    META_MIME_TYPE, request.mimeType().value(),
                    META_STORED_AT, storedAt.toString(),
                    META_TENANT_ID, request.tenantId().value(),
                    META_DOCUMENT_ID, request.documentId().value()
            );

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioConfig.bucketName())
                                .object(request.objectKey().value())
                                .stream(inputStream, sizeBytes, -1)
                                .contentType(request.mimeType().value())
                                .userMetadata(metadata)
                                .build()
                );
            }

            return new StoredDocumentObject(
                    request.tenantId(),
                    request.documentId(),
                    request.objectKey(),
                    request.fileName(),
                    request.mimeType(),
                    sizeBytes,
                    contentHash,
                    STORAGE_PROVIDER,
                    storedAt
            );
        } catch (Exception exception) {
            throw new DocumentServiceException(
                    "Failed to store document object: " + request.objectKey().value(),
                    exception
            );
        }
    }

    @Override
    public byte[] read(String objectKey) {
        requireText(objectKey, "objectKey");

        try {
            try (var objectStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.bucketName())
                            .object(objectKey)
                            .build()
            )) {
                return objectStream.readAllBytes();
            }
        } catch (ErrorResponseException exception) {
            if (isNotFound(exception)) {
                throw new DocumentServiceException("Object not found: " + objectKey, exception);
            }

            throw new DocumentServiceException(
                    "Failed to read document object: " + objectKey,
                    exception
            );
        } catch (Exception exception) {
            throw new DocumentServiceException(
                    "Failed to read document object: " + objectKey,
                    exception
            );
        }
    }

    @Override
    public boolean exists(String objectKey) {
        requireText(objectKey, "objectKey");

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.bucketName())
                            .object(objectKey)
                            .build()
            );

            return true;
        } catch (ErrorResponseException exception) {
            if (isNotFound(exception)) {
                return false;
            }

            throw new DocumentServiceException(
                    "Failed to check document object existence: " + objectKey,
                    exception
            );
        } catch (Exception exception) {
            throw new DocumentServiceException(
                    "Failed to check document object existence: " + objectKey,
                    exception
            );
        }
    }

    @Override
    public void delete(String objectKey) {
        requireText(objectKey, "objectKey");

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.bucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (ErrorResponseException exception) {
            if (isNotFound(exception)) {
                return;
            }

            throw new DocumentServiceException(
                    "Failed to delete document object: " + objectKey,
                    exception
            );
        } catch (Exception exception) {
            throw new DocumentServiceException(
                    "Failed to delete document object: " + objectKey,
                    exception
            );
        }
    }

    public MinioObjectMetadata metadata(ObjectKey objectKey) {
        if (objectKey == null) {
            throw InvalidDocumentException.blankField("objectKey");
        }

        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.bucketName())
                            .object(objectKey.value())
                            .build()
            );

            Map<String, String> metadata = stat.userMetadata();

            String fileName = metadataValue(metadata, META_FILE_NAME);
            String mimeType = metadataValue(metadata, META_MIME_TYPE);
            String contentHash = metadataValue(metadata, META_CONTENT_HASH);

            Instant storedAt = stat.lastModified() == null
                    ? Instant.now()
                    : stat.lastModified().toInstant();

            return new MinioObjectMetadata(
                    minioConfig.bucketName(),
                    objectKey,
                    com.sparrowx.document.domain.valueobjects.FileName.of(fileName),
                    com.sparrowx.document.domain.valueobjects.MimeType.of(mimeType),
                    stat.size(),
                    ContentHash.of(contentHash),
                    storedAt
            );
        } catch (ErrorResponseException exception) {
            if (isNotFound(exception)) {
                throw new DocumentServiceException(
                        "Object metadata not found: " + objectKey.value(),
                        exception
                );
            }

            throw new DocumentServiceException(
                    "Failed to read object metadata: " + objectKey.value(),
                    exception
            );
        } catch (Exception exception) {
            throw new DocumentServiceException(
                    "Failed to read object metadata: " + objectKey.value(),
                    exception
            );
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.bucketName())
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.bucketName())
                                .build()
                );
            }
        } catch (Exception exception) {
            throw new DocumentServiceException(
                    "Failed to ensure MinIO bucket exists: " + minioConfig.bucketName(),
                    exception
            );
        }
    }

    private boolean isNotFound(ErrorResponseException exception) {
        String code = exception.errorResponse() == null
                ? null
                : exception.errorResponse().code();

        return "NoSuchKey".equals(code)
                || "NoSuchBucket".equals(code)
                || "NoSuchObject".equals(code)
                || "NotFound".equals(code);
    }

    private String metadataValue(
            Map<String, String> metadata,
            String key
    ) {
        if (metadata == null || metadata.isEmpty()) {
            throw new DocumentServiceException("Object metadata is empty");
        }

        String value = metadata.get(key);

        if (value == null) {
            value = metadata.get("x-amz-meta-" + key);
        }

        if (value == null || value.isBlank()) {
            throw new DocumentServiceException("Object metadata field missing: " + key);
        }

        return value;
    }

    private void validate(StoreDocumentObjectRequest request) {
        if (request == null) {
            throw InvalidDocumentException.nullCommand("StoreDocumentObjectRequest");
        }

        if (request.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (request.documentId() == null) {
            throw InvalidDocumentException.blankField("documentId");
        }

        if (request.objectKey() == null) {
            throw InvalidDocumentException.blankField("objectKey");
        }

        if (request.fileName() == null) {
            throw InvalidDocumentException.blankField("fileName");
        }

        if (request.mimeType() == null) {
            throw InvalidDocumentException.blankField("mimeType");
        }

        if (request.content() == null || request.content().length == 0) {
            throw InvalidDocumentException.emptyContent();
        }
    }

    private void requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw InvalidDocumentException.blankField(fieldName);
        }
    }
}