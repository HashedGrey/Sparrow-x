package com.sparrowx.internal.features.getinternalgraphcontext;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.internal.data.neo4j.Neo4jGraphReadClient;
import com.sparrowx.internal.valueobjects.InternalGraphNodeType;
import com.sparrowx.internal.valueobjects.InternalGraphType;
import org.springframework.stereotype.Component;

@Component
public class GetInternalGraphContextQueryHandler
        implements QueryHandler<GetInternalGraphContextQuery, GetInternalGraphContextResult> {

    private final Neo4jGraphReadClient graphReadClient;
    private final GetInternalGraphContextQueryValidator validator;

    public GetInternalGraphContextQueryHandler(
            Neo4jGraphReadClient graphReadClient,
            GetInternalGraphContextQueryValidator validator
    ) {
        this.graphReadClient = graphReadClient;
        this.validator = validator;
    }

    @Override
    public GetInternalGraphContextResult handle(
            GetInternalGraphContextQuery query
    ) {
        validator.validate(query);

        var graphType = InternalGraphType.from(query.graphType());
        var rootNodeType = InternalGraphNodeType.from(query.rootNodeType());

        var graph = graphReadClient.read(
                graphType,
                query.tenantId(),
                query.rootEntityId(),
                rootNodeType,
                normalizeDepth(query.depth()),
                normalizeLimit(query.limit())
        );

        return new GetInternalGraphContextResult(graph);
    }

    private int normalizeDepth(int depth) {
        return depth <= 0 ? 1 : depth;
    }

    private int normalizeLimit(int limit) {
        return limit <= 0 ? 50 : limit;
    }
}