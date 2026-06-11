package com.sparrowx.apigateway.features.search;

import buildingblocks.core.queries.QueryBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.sparrowx.apigateway.features.search.query.SearchQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/search")
@Tag(name = "search")
public class SearchController {

    private final QueryBus queryBus;
    private final SearchGatewayMapper searchGatewayMapper;

    public SearchController(QueryBus queryBus, SearchGatewayMapper searchGatewayMapper) {
        this.queryBus = queryBus;
        this.searchGatewayMapper = searchGatewayMapper;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search across Sparrowx resources")
    public ResponseEntity<SearchResultDto> search(
            @RequestParam("q") String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        SearchRequestDto requestDto = SearchRequestDto.builder()
                .query(query)
                .type(type)
                .page(page)
                .size(size)
                .build();

        SearchQuery searchQuery = searchGatewayMapper.toSearchQuery(requestDto);

        SearchResult searchResult = queryBus.dispatch(searchQuery);

        SearchResultDto response = searchGatewayMapper.toDto(searchResult);

        return ResponseEntity.ok(response);
    }
}