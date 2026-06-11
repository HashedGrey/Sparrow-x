package com.sparrowx.apigateway.features.search;

import com.sparrowx.apigateway.features.search.query.SearchQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchGatewayMapper {

    public SearchQuery toSearchQuery(SearchRequestDto dto) {
        return new SearchQuery(
                dto.getQuery(),
                dto.getType(),
                dto.getPage() == null ? 0 : dto.getPage(),
                dto.getSize() == null ? 20 : dto.getSize()
        );
    }

    public SearchResultDto toDto(SearchResult result) {
        List<SearchItemDto> items = result.getItems()
                .stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        return new SearchResultDto(
                items,
                result.getPage(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private SearchItemDto toItemDto(SearchItem item) {
        return new SearchItemDto(
                item.getId(),
                item.getType(),
                item.getTitle(),
                item.getSnippet(),
                item.getUri(),
                item.getScore()
        );
    }
}