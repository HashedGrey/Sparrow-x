package com.sparrowx.agentic.steps;

import com.sparrowx.agentic.actions.internal.ReadInternalCompanyGraphAction;
import com.sparrowx.agentic.actions.internal.ReadLearningGraphAction;
import com.sparrowx.agentic.actions.internal.SearchInternalEntitiesAction;
import com.sparrowx.agentic.mission.evidence.EvidenceRef;
import com.sparrowx.agentic.tools.internal.InternalEntitySearchRequestBuilder.SearchSpec;
import com.sparrowx.agentic.tools.internal.InternalGraphRequestBuilder.GraphSpec;
import com.sparrowx.agentic.validation.DownstreamResponseValidator;
import com.sparrowx.agentic.validation.DownstreamResponseValidator.ResponseMetadata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public final class ResolveInternalContextStep {

    private final SearchInternalEntitiesAction searchAction;
    private final ReadInternalCompanyGraphAction companyGraphAction;
    private final ReadLearningGraphAction learningGraphAction;
    private final DownstreamResponseValidator responseValidator;

    public ResolveInternalContextStep(
            SearchInternalEntitiesAction searchAction,
            ReadInternalCompanyGraphAction companyGraphAction,
            ReadLearningGraphAction learningGraphAction,
            DownstreamResponseValidator responseValidator
    ) {
        this.searchAction = Objects.requireNonNull(
                searchAction,
                "searchAction must not be null"
        );
        this.companyGraphAction = Objects.requireNonNull(
                companyGraphAction,
                "companyGraphAction must not be null"
        );
        this.learningGraphAction = Objects.requireNonNull(
                learningGraphAction,
                "learningGraphAction must not be null"
        );
        this.responseValidator = Objects.requireNonNull(
                responseValidator,
                "responseValidator must not be null"
        );
    }

    public Result execute(
            MissionContext context,
            Request request
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(request, "request must not be null");

        return switch (request.operation()) {
            case SEARCH_ENTITIES -> search(context, request.searchSpec());
            case READ_COMPANY_GRAPH ->
                    readCompanyGraph(context, request.graphSpec());
            case READ_LEARNING_GRAPH ->
                    readLearningGraph(context, request.graphSpec());
        };
    }

    private Result search(
            MissionContext context,
            SearchSpec spec
    ) {
        SearchInternalEntitiesAction.Result result =
                searchAction.execute(context, spec);

        responseValidator.validateInternal(
                "search-internal-entities",
                context.tenantId(),
                result,
                value -> new ResponseMetadata(
                        context.tenantId(),
                        spec.requestId(),
                        spec.requestId() + ":internal-entities",
                        value.candidates().size(),
                        value.candidates().stream()
                                .mapToLong(candidate ->
                                        candidate.getSerializedSize())
                                .sum(),
                        value.evidenceRefs()
                )
        );

        return new Result(
                Operation.SEARCH_ENTITIES,
                result.evidenceRefs(),
                "Resolved "
                        + result.candidates().size()
                        + " internal entity candidates",
                result.warnings(),
                Map.of(
                        "candidateCount", result.candidates().size(),
                        "ambiguous", result.ambiguous()
                )
        );
    }

    private Result readCompanyGraph(
            MissionContext context,
            GraphSpec spec
    ) {
        ReadInternalCompanyGraphAction.Result result =
                companyGraphAction.execute(context, spec);

        responseValidator.validateInternal(
                "read-internal-company-graph",
                context.tenantId(),
                result,
                value -> new ResponseMetadata(
                        context.tenantId(),
                        spec.requestId(),
                        spec.requestId() + ":company-graph",
                        value.evidenceRefs().size(),
                        value.graph().getSerializedSize(),
                        value.evidenceRefs()
                )
        );

        return new Result(
                Operation.READ_COMPANY_GRAPH,
                result.evidenceRefs(),
                "Loaded internal company graph context",
                List.of(),
                Map.of(
                        "serializedSizeBytes",
                        result.graph().getSerializedSize()
                )
        );
    }

    private Result readLearningGraph(
            MissionContext context,
            GraphSpec spec
    ) {
        ReadLearningGraphAction.Result result =
                learningGraphAction.execute(context, spec);

        responseValidator.validateInternal(
                "read-learning-graph",
                context.tenantId(),
                result,
                value -> new ResponseMetadata(
                        context.tenantId(),
                        spec.requestId(),
                        spec.requestId() + ":learning-graph",
                        value.evidenceRefs().size(),
                        value.graph().getSerializedSize(),
                        value.evidenceRefs()
                )
        );

        return new Result(
                Operation.READ_LEARNING_GRAPH,
                result.evidenceRefs(),
                "Loaded learning graph context",
                List.of(),
                Map.of(
                        "serializedSizeBytes",
                        result.graph().getSerializedSize()
                )
        );
    }

    public record Request(
            Operation operation,
            SearchSpec searchSpec,
            GraphSpec graphSpec
    ) {
        public Request {
            operation = Objects.requireNonNull(
                    operation,
                    "operation must not be null"
            );

            if (operation == Operation.SEARCH_ENTITIES) {
                Objects.requireNonNull(
                        searchSpec,
                        "searchSpec is required for SEARCH_ENTITIES"
                );
                if (graphSpec != null) {
                    throw new IllegalArgumentException(
                            "graphSpec is not allowed for SEARCH_ENTITIES"
                    );
                }
            } else {
                Objects.requireNonNull(
                        graphSpec,
                        "graphSpec is required for graph operations"
                );
                if (searchSpec != null) {
                    throw new IllegalArgumentException(
                            "searchSpec is not allowed for graph operations"
                    );
                }
            }
        }
    }

    public record Result(
            Operation operation,
            List<EvidenceRef> evidenceRefs,
            String summary,
            List<String> warnings,
            Map<String, Object> attributes
    ) {
        public Result {
            operation = Objects.requireNonNull(
                    operation,
                    "operation must not be null"
            );
            evidenceRefs = evidenceRefs == null
                    ? List.of()
                    : List.copyOf(evidenceRefs);
            summary = summary == null ? "" : summary;
            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);
            attributes = attributes == null
                    ? Map.of()
                    : Map.copyOf(attributes);
        }
    }

    public enum Operation {
        SEARCH_ENTITIES,
        READ_COMPANY_GRAPH,
        READ_LEARNING_GRAPH
    }
}