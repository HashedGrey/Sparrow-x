package com.sparrowx.document.features.getingestionjob;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.document.data.DocumentPersistenceMapper;
import com.sparrowx.document.data.postgres.entities.IngestionJobEntity;
import com.sparrowx.document.data.postgres.repositories.IngestionJobRepository;
import com.sparrowx.document.domain.models.IngestionJob;
import com.sparrowx.document.exceptions.IngestionJobNotFoundException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetIngestionJobQueryHandler
        implements QueryHandler<GetIngestionJobQuery, GetIngestionJobResult> {

    private final IngestionJobRepository ingestionJobRepository;
    private final DocumentPersistenceMapper documentPersistenceMapper;

    public GetIngestionJobQueryHandler(
            IngestionJobRepository ingestionJobRepository,
            DocumentPersistenceMapper documentPersistenceMapper
    ) {
        this.ingestionJobRepository = ingestionJobRepository;
        this.documentPersistenceMapper = documentPersistenceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public GetIngestionJobResult handle(GetIngestionJobQuery query) {
        validate(query);

        IngestionJobEntity entity = ingestionJobRepository
                .findByIngestionJobIdAndTenantId(
                        query.ingestionJobId().value(),
                        query.tenantId().value()
                )
                .orElseThrow(() -> new IngestionJobNotFoundException(
                        query.ingestionJobId().value()
                ));

        IngestionJob job = documentPersistenceMapper.toDomain(entity);

        return new GetIngestionJobResult(job);
    }

    private void validate(GetIngestionJobQuery query) {
        if (query == null) {
            throw InvalidDocumentException.nullQuery("GetIngestionJobQuery");
        }

        if (query.requestId() == null) {
            throw InvalidDocumentException.blankField("requestId");
        }

        if (query.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (query.userId() == null) {
            throw InvalidDocumentException.blankField("userId");
        }

        if (query.ingestionJobId() == null) {
            throw InvalidDocumentException.blankField("ingestionJobId");
        }
    }
}