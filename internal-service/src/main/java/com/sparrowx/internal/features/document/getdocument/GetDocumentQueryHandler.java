package com.sparrowx.internal.features.document.getdocument;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.postgres.mappers.InternalDocumentPersistenceMapper;
import com.sparrowx.internal.data.postgres.repositories.InternalDocumentJpaRepository;
import com.sparrowx.internal.exceptions.InternalNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetDocumentQueryHandler
        implements QueryHandler<GetDocumentQuery, GetDocumentResult> {

    private final InternalDocumentJpaRepository internalDocumentJpaRepository;
    private final GetDocumentQueryValidator validator;

    public GetDocumentQueryHandler(
            InternalDocumentJpaRepository internalDocumentJpaRepository,
            GetDocumentQueryValidator validator
    ) {
        this.internalDocumentJpaRepository = internalDocumentJpaRepository;
        this.validator = validator;
    }

    @Override
    public GetDocumentResult handle(GetDocumentQuery query) {
        validator.validate(query);

        var document = internalDocumentJpaRepository
                .findByTenantIdAndDocumentId(
                        query.tenantId(),
                        query.documentId()
                )
                .map(InternalDocumentPersistenceMapper::toDomain)
                .orElseThrow(() -> new InternalNotFoundException(
                        "Internal document not found: " + query.documentId()
                ));

        return new GetDocumentResult(document);
    }
}