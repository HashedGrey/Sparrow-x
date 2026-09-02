package com.sparrowx.document.features.searchdocumentspans;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.document.domain.models.RetrievalEvidence;
import com.sparrowx.document.domain.models.SourceSpan;
import com.sparrowx.document.domain.valueobjects.DocumentId;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.exceptions.RetrievalFailedException;
import com.sparrowx.document.observability.RetrievalLifecycleLogger;
import com.sparrowx.document.retrieval.DocumentScopeResolver;
import com.sparrowx.document.retrieval.HybridDocumentRetriever;
import com.sparrowx.document.retrieval.SourceSpanBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SearchDocumentSpansQueryHandler
        implements QueryHandler<SearchDocumentSpansQuery, SearchDocumentSpansResult> {

    private final HybridDocumentRetriever hybridDocumentRetriever;
    private final SourceSpanBuilder sourceSpanBuilder;
    private final RetrievalLifecycleLogger retrievalLifecycleLogger;
    private final DocumentScopeResolver documentScopeResolver;

    public SearchDocumentSpansQueryHandler(
            HybridDocumentRetriever hybridDocumentRetriever,
            SourceSpanBuilder sourceSpanBuilder,
            RetrievalLifecycleLogger retrievalLifecycleLogger,
            DocumentScopeResolver documentScopeResolver
    ) {
        this.hybridDocumentRetriever = hybridDocumentRetriever;
        this.sourceSpanBuilder = sourceSpanBuilder;
        this.retrievalLifecycleLogger = retrievalLifecycleLogger;
        this.documentScopeResolver = documentScopeResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public SearchDocumentSpansResult handle(SearchDocumentSpansQuery query) {
        validate(query);

        Set<DocumentId> documentIds = documentScopeResolver.resolve(
                query.tenantId(),
                query.scope().documentIds(),
                query.scope().fileNames()
        );

        validateUnsupportedScope(query.scope(), documentIds);

        List<String> warnings = buildScopeWarnings(query.scope());
        retrievalLifecycleLogger.searchRequested(
                query.tenantId(),
                query.userId(),
                query.projectId(),
                query.teamId(),
                query.retrievalMode(),
                query.limit(),
                documentIds.size()
        );

        try {
            List<RetrievalEvidence> evidence = hybridDocumentRetriever.retrieve(
                    new HybridDocumentRetriever.RetrieveDocumentsRequest(
                            query.tenantId(),
                            query.userId(),
                            query.projectId(),
                            query.teamId(),
                            query.query(),
                            query.limit(),
                            query.retrievalMode(),
                            documentIds
                    )
            );

            List<SourceSpan> spans = evidence.stream()
                    .map(item -> sourceSpanBuilder.fromRetrievalEvidence(
                            item,
                            query.includeExcerpts()
                    ))
                    .toList();

            retrievalLifecycleLogger.searchCompleted(
                    query.tenantId(),
                    query.userId(),
                    query.retrievalMode(),
                    spans.size()
            );

            return new SearchDocumentSpansResult(
                    spans,
                    calculateCoverageScore(spans, query.limit()),
                    warnings
            );

        } catch (RuntimeException exception) {
            retrievalLifecycleLogger.searchFailed(
                    query.tenantId(),
                    query.userId(),
                    query.retrievalMode(),
                    exception.getMessage()
            );

            if (exception instanceof RetrievalFailedException) {
                throw exception;
            }

            throw new RetrievalFailedException(
                    "Search document spans failed",
                    exception
            );
        }
    }
    private void validateUnsupportedScope(
            SearchDocumentSpansQuery.DocumentScope scope,
            Set<DocumentId> resolvedDocumentIds
    ) {
        if (scope == null || !resolvedDocumentIds.isEmpty()) {
            return;
        }

        boolean hasUnsupportedScope =
                !scope.collectionIds().isEmpty()
                        || !scope.tags().isEmpty()
                        || !scope.metadataFilters().isEmpty();

        if (hasUnsupportedScope) {
            throw InvalidDocumentException.unsupportedScopeOnly();
        }
    }

    private List<String> buildScopeWarnings(SearchDocumentSpansQuery.DocumentScope scope) {
        List<String> warnings = new ArrayList<>();

        if (scope == null) {
            return warnings;
        }

        if (!scope.collectionIds().isEmpty()) {
            warnings.add("collection_ids scope is accepted by API but not yet enforced in SearchDocumentSpansQueryHandler.");
        }

        if (!scope.tags().isEmpty()) {
            warnings.add("tags scope is accepted by API but not yet enforced in SearchDocumentSpansQueryHandler.");
        }

        if (!scope.metadataFilters().isEmpty()) {
            warnings.add("metadata_filters scope is accepted by API but not yet enforced in SearchDocumentSpansQueryHandler.");
        }

        return warnings;
    }

    private double calculateCoverageScore(
            List<SourceSpan> spans,
            int requestedLimit
    ) {
        if (spans == null || spans.isEmpty()) {
            return 0.0;
        }

        int denominator = requestedLimit <= 0 ? 10 : requestedLimit;

        return Math.min(1.0, (double) spans.size() / denominator);
    }

    private void validate(SearchDocumentSpansQuery query) {
        if (query == null) {
            throw InvalidDocumentException.nullQuery("SearchDocumentSpansQuery");
        }

        if (query.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (query.userId() == null) {
            throw InvalidDocumentException.blankField("userId");
        }

        if (query.query() == null || query.query().value() == null || query.query().value().isBlank()) {
            throw InvalidDocumentException.blankField("query");
        }

        if (query.scope() == null) {
            throw InvalidDocumentException.blankField("scope");
        }

        if (query.limit() < 0) {
            throw InvalidDocumentException.blankField("limit");
        }
    }
}