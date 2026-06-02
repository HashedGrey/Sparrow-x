package com.sparrowx.document.data.postgres.entities;

import com.sparrowx.document.domain.valueobjects.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @Column(name = "document_id", nullable = false, updatable = false)
    private String documentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "title")
    private String title;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by_user_id", nullable = false)
    private String createdByUserId;

}