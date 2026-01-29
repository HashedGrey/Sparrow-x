package com.sparrowx.apigateway.features.agentic;

import buildingblocks.core.queries.QueryBus;
import com.sparrowx.apigateway.dtos.AgenticRequestDto;
import com.sparrowx.apigateway.dtos.AgenticResultDto;
import com.sparrowx.apigateway.features.agentic.query.AgenticQuery;
import com.sparrowx.apigateway.mappers.AgenticGatewayMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/agentic")
@Tag(name = "agentic")
public class AgenticController {

    private final QueryBus queryBus;
    private final AgenticGatewayMapper agenticGatewayMapper;

    public AgenticController(QueryBus queryBus, AgenticGatewayMapper agenticGatewayMapper) {
        this.queryBus = queryBus;
        this.agenticGatewayMapper = agenticGatewayMapper;
    }

    @PostMapping("/query")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AgenticResultDto> query(@RequestBody AgenticRequestDto requestDto) {
        // Map REST -> QueryBus query
        AgenticQuery agenticQuery = agenticGatewayMapper.toAgenticQuery(requestDto);

        // Send to QueryBus
        AgenticResultDto agenticResultDto = queryBus.send(agenticQuery);

        return ResponseEntity.ok(agenticResultDto);
    }
}
