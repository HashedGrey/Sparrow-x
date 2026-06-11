package com.sparrowx.apigateway.features.search.query;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.apigateway.features.search.SearchResult;
import com.sparrowx.apigateway.grpc.clients.SearchGrpcClient;
import org.springframework.stereotype.Component;

@Component
public class SearchQueryHandler implements QueryHandler<SearchQuery, SearchResult> {

    private final SearchGrpcClient searchGrpcClient;

    public SearchQueryHandler(SearchGrpcClient searchGrpcClient) {
        this.searchGrpcClient = searchGrpcClient;
    }

    @Override
    public SearchResult handle(SearchQuery query) {
        return searchGrpcClient.search(
                query.getQuery(),
                query.getType(),
                query.getPage(),
                query.getSize()
        );
    }
}