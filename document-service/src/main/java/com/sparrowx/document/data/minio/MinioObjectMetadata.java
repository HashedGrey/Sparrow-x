package com.sparrowx.document.data.minio;

import com.sparrowx.document.domain.valueobjects.ContentHash;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;

import java.time.Instant;

public record MinioObjectMetadata(
        String bucketName,
        ObjectKey objectKey,
        FileName fileName,
        MimeType mimeType,
        long sizeBytes,
        ContentHash contentHash,
        Instant storedAt
) {
}