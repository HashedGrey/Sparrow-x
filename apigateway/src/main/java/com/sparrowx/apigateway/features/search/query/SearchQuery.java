package com.sparrowx.apigateway.features.search.query;

import buildingblocks.core.queries.Query;
import com.sparrowx.apigateway.features.search.SearchResult;
import lombok.Getter;

@Getter
public class SearchQuery implements Query<SearchResult> {

    private final String query;
    private final String type;
    private final int page;
    private final int size;

    public SearchQuery(String query, String type, int page, int size) {
        this.query = query;
        this.type = type;
        this.page = page;
        this.size = size;
    }

}