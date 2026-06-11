package com.sparrowx.apigateway.grpc.clients;

import com.sparrowx.apigateway.features.search.SearchItem;
import com.sparrowx.apigateway.features.search.SearchResult;
import io.grpc.ManagedChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class SearchGrpcClient {

    public SearchGrpcClient(ManagedChannel channel) {
    }

    public SearchResult search(String query, String type, int page, int size) {
        // Temporary fake implementation until search-service.proto and generated gRPC stubs exist.
        // This lets the gateway /search route work in Postman now.
        // The channel is intentionally unused for now, but kept so this client is ready for real gRPC later.

        List<SearchItem> seed = seedData();

        List<SearchItem> filtered = seed.stream()
                .filter(item -> matchesQuery(item, query))
                .filter(item -> matchesType(item, type))
                .collect(Collectors.toList());

        long totalElements = filtered.size();
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<SearchItem> pagedItems = filtered.subList(fromIndex, toIndex);

        return new SearchResult(
                pagedItems,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    private boolean matchesQuery(SearchItem item, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String q = query.toLowerCase(Locale.ROOT);

        return contains(item.getTitle(), q)
                || contains(item.getSnippet(), q)
                || contains(item.getType(), q)
                || contains(item.getUri(), q);
    }

    private boolean matchesType(SearchItem item, String type) {
        if (type == null || type.isBlank()) {
            return true;
        }

        return item.getType() != null
                && item.getType().equalsIgnoreCase(type);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private List<SearchItem> seedData() {
        List<SearchItem> items = new ArrayList<>();

        items.add(new SearchItem(
                "tweet-101",
                "tweet",
                "World Cup final analysis",
                "A breakdown of the final match and fan reactions.",
                "/tweets/tweet-101",
                0.98
        ));

        items.add(new SearchItem(
                "tweet-102",
                "tweet",
                "Redis caching for hot timelines",
                "How timeline fanout can use Redis for low-latency reads.",
                "/tweets/tweet-102",
                0.94
        ));

        items.add(new SearchItem(
                "profile-201",
                "profile",
                "Aggrey Lelei",
                "Backend engineer focused on distributed systems and microservices.",
                "/profiles/profile-201",
                0.91
        ));

        items.add(new SearchItem(
                "profile-202",
                "profile",
                "SparrowX Team",
                "Official profile for SparrowX platform updates.",
                "/profiles/profile-202",
                0.88
        ));

        items.add(new SearchItem(
                "tweet-103",
                "tweet",
                "Elasticsearch ranking notes",
                "Notes on score tuning, filtering, and retrieval strategy.",
                "/tweets/tweet-103",
                0.89
        ));

        return items;
    }
}