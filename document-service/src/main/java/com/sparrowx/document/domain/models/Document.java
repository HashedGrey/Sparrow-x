package com.sparrowx.document.domain.models;

import com.sparrowx.document.domain.valueobjects.ContentHash;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.DocumentStatus;
import com.sparrowx.document.domain.valueobjects.DocumentTitle;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.UserId;

import java.time.Instant;

public record Document(
        DocumentId documentId,
        TenantId tenantId,
        ProjectId projectId,
        TeamId teamId,
        DocumentTitle title,
        FileName fileName,
        MimeType mimeType,
        long sizeBytes,
        ObjectKey objectKey,
        ContentHash contentHash,
        DocumentStatus status,
        Instant createdAt,
        Instant updatedAt,
        UserId createdByUserId
) {
}