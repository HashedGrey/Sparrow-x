package com.sparrowx.document.data.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "document_chunks")
public class DocumentChunkEntity {

    @Id
    @Column(name = "chunk_id", nullable = false, updatable = false)
    private String chunkId;

    @Column(name = "document_id", nullable = false)
    private String documentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "page_start", nullable = false)
    private int pageStart;

    @Column(name = "page_end", nullable = false)
    private int pageEnd;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}