package com.sparrowx.apigateway.features.agentic.query;

import buildingblocks.core.queries.IQuery;
import com.sparrowx.apigateway.dtos.AgenticResultDto;


public record AgenticQuery(
        String userId,
        String prompt
        //private Integer maxTokens;
        //private Double temperature;
) implements IQuery<AgenticResultDto> {
}
