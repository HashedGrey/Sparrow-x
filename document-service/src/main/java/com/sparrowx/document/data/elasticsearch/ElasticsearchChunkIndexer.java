package com.sparrowx.document.data.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import com.sparrowx.document.config.ElasticsearchConfig;
import com.sparrowx.document.domain.valueobjects.ChunkId;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.exceptions.DocumentIndexingException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ElasticsearchChunkIndexer {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchConfig.ElasticsearchProperties properties;

    public ElasticsearchChunkIndexer(
            ElasticsearchClient elasticsearchClient,
            ElasticsearchConfig.ElasticsearchProperties properties
    ) {
        this.elasticsearchClient = elasticsearchClient;
        this.properties = properties;
    }

    @PostConstruct
    public void ensureIndexExists() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(request -> request.index(properties.indexName()))
                    .value();

            if (exists) {
                return;
            }

            String mapping = """
                    {
                      "mappings": {
                        "properties": {
                          "tenant_id": { "type": "keyword" },
                          "project_id": { "type": "keyword" },
                          "team_id": { "type": "keyword" },
                          "document_id": { "type": "keyword" },
                          "chunk_id": { "type": "keyword" },
                          "text": { "type": "text" },
                          "chunk_index": { "type": "integer" },
                          "page_start": { "type": "integer" },
                          "page_end": { "type": "integer" },
                          "metadata": { "type": "object", "enabled": true },
                          "indexed_at": { "type": "date" }
                        }
                      }
                    }
                    """;

            elasticsearchClient.indices().create(request -> request
                    .index(properties.indexName())
                    .withJson(new StringReader(mapping))
            );

        } catch (Exception exception) {
            throw new DocumentIndexingException(
                    "Failed to create Elasticsearch index: " + properties.indexName(),
                    exception
            );
        }
    }

    public void indexChunk(
            TenantId tenantId,
            ProjectId projectId,
            TeamId teamId,
            DocumentId documentId,
            ChunkId chunkId,
            String text,
            int chunkIndex,
            int pageStart,
            int pageEnd,
            Map<String, String> metadata
    ) {
        validate(tenantId, documentId, chunkId, text);

        try {
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("tenant_id", tenantId.value());
            document.put("project_id", projectId == null ? "" : projectId.value());
            document.put("team_id", teamId == null ? "" : teamId.value());
            document.put("document_id", documentId.value());
            document.put("chunk_id", chunkId.value());
            document.put("text", text);
            document.put("chunk_index", chunkIndex);
            document.put("page_start", pageStart);
            document.put("page_end", pageEnd);
            document.put("metadata", metadata == null ? Map.of() : Map.copyOf(metadata));
            document.put("indexed_at", Instant.now().toString());

            elasticsearchClient.index(request -> request
                    .index(properties.indexName())
                    .id(chunkId.value())
                    .document(document)
                    .refresh(Refresh.WaitFor)
            );

        } catch (Exception exception) {
            throw new DocumentIndexingException(
                    "Failed to index chunk in Elasticsearch: chunkId=" + chunkId.value(),
                    exception
            );
        }
    }

    private void validate(
            TenantId tenantId,
            DocumentId documentId,
            ChunkId chunkId,
            String text
    ) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(chunkId, "chunkId must not be null");

        if (text == null || text.isBlank()) {
            throw InvalidDocumentException.blankField("text");
        }
    }
}