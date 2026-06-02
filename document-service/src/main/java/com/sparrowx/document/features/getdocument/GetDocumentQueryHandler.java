package com.sparrowx.document.features.getdocument;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.document.data.DocumentPersistenceMapper;
import com.sparrowx.document.data.postgres.entities.DocumentEntity;
import com.sparrowx.document.data.postgres.repositories.DocumentRepository;
import com.sparrowx.document.domain.models.Document;
import com.sparrowx.document.exceptions.DocumentNotFoundException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetDocumentQueryHandler implements QueryHandler<GetDocumentQuery, GetDocumentResult> {

    private final DocumentRepository documentRepository;
    private final DocumentPersistenceMapper documentPersistenceMapper;

    public GetDocumentQueryHandler(
            DocumentRepository documentRepository,
            DocumentPersistenceMapper documentPersistenceMapper
    ) {
        this.documentRepository = documentRepository;
        this.documentPersistenceMapper = documentPersistenceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public GetDocumentResult handle(GetDocumentQuery query) {
        validate(query);

        DocumentEntity entity = documentRepository
                .findByDocumentIdAndTenantId(
                        query.documentId().value(),
                        query.tenantId().value()
                )
                .orElseThrow(() -> new DocumentNotFoundException(
                        query.documentId().value(),
                        query.tenantId().value()
                ));

        Document document = documentPersistenceMapper.toDomain(entity);

        return new GetDocumentResult(document);
    }

    private void validate(GetDocumentQuery query) {
        if (query == null) {
            throw InvalidDocumentException.nullQuery("GetDocumentQuery");
        }

        if (query.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (query.documentId() == null) {
            throw InvalidDocumentException.blankField("documentId");
        }
    }
}