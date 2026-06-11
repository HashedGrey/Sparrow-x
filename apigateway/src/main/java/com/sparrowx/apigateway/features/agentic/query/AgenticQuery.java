package com.sparrowx.apigateway.features.agentic.query;

import buildingblocks.core.queries.Query;
import com.sparrowx.apigateway.features.agentic.AgenticResultDto;


public record AgenticQuery(
        String userId,
        String prompt
        //private Integer maxTokens;
        //private Double temperature;
) implements Query<AgenticResultDto> {
}
