package com.sparrowx.internal.data.neo4j;

import com.sparrowx.internal.data.neo4j.queries.InternalAgentGraphQuery;
import com.sparrowx.internal.data.neo4j.queries.InternalGraphPathQuery;
import com.sparrowx.internal.data.neo4j.queries.LearningGraphQuery;
import com.sparrowx.internal.models.InternalGraphContext;
import com.sparrowx.internal.valueobjects.InternalGraphNodeType;
import com.sparrowx.internal.valueobjects.InternalGraphType;
import org.springframework.stereotype.Component;

@Component
public class Neo4jGraphReadClient {

    private final InternalAgentGraphQuery internalAgentGraphQuery;
    private final LearningGraphQuery learningGraphQuery;
    private final InternalGraphPathQuery internalGraphPathQuery;

    public Neo4jGraphReadClient(
            InternalAgentGraphQuery internalAgentGraphQuery,
            LearningGraphQuery learningGraphQuery,
            InternalGraphPathQuery internalGraphPathQuery
    ) {
        this.internalAgentGraphQuery = internalAgentGraphQuery;
        this.learningGraphQuery = learningGraphQuery;
        this.internalGraphPathQuery = internalGraphPathQuery;
    }

    public InternalGraphContext readCompanyGraph(
            String tenantId,
            String rootEntityId,
            InternalGraphNodeType rootNodeType,
            int depth,
            int limit
    ) {
        return internalAgentGraphQuery.read(
                tenantId,
                rootEntityId,
                rootNodeType,
                depth,
                limit
        );
    }

    public InternalGraphContext readLearningGraph(
            String tenantId,
            String rootEntityId,
            InternalGraphNodeType rootNodeType,
            int depth,
            int limit
    ) {
        return learningGraphQuery.read(
                tenantId,
                rootEntityId,
                rootNodeType,
                depth,
                limit
        );
    }

    public InternalGraphContext read(
            InternalGraphType graphType,
            String tenantId,
            String rootEntityId,
            InternalGraphNodeType rootNodeType,
            int depth,
            int limit
    ) {
        return switch (graphType) {
            case COMPANY -> readCompanyGraph(
                    tenantId,
                    rootEntityId,
                    rootNodeType,
                    depth,
                    limit
            );
            case LEARNING -> readLearningGraph(
                    tenantId,
                    rootEntityId,
                    rootNodeType,
                    depth,
                    limit
            );
        };
    }

    public InternalGraphContext explainPath(
            String tenantId,
            String sourceEntityId,
            InternalGraphNodeType sourceNodeType,
            String targetEntityId,
            InternalGraphNodeType targetNodeType,
            int maxDepth
    ) {
        return internalGraphPathQuery.explainPath(
                tenantId,
                sourceEntityId,
                sourceNodeType,
                targetEntityId,
                targetNodeType,
                maxDepth
        );
    }
}