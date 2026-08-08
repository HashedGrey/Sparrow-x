package com.sparrowx.agentic.adapters.llm;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class LlmFallbackPolicy {

    private final List<ModelRoute> configuredRoutes;

    public LlmFallbackPolicy(List<ModelRoute> configuredRoutes) {
        Objects.requireNonNull(configuredRoutes, "configuredRoutes must not be null");

        Set<String> routeKeys = new HashSet<>();
        this.configuredRoutes = configuredRoutes.stream()
                .filter(ModelRoute::enabled)
                .peek(route -> {
                    if (!routeKeys.add(route.key())) {
                        throw new IllegalArgumentException("duplicate LLM route: " + route.key());
                    }
                })
                .sorted(Comparator.comparingInt(ModelRoute::priority)
                        .thenComparing(ModelRoute::provider)
                        .thenComparing(ModelRoute::model))
                .toList();

        if (this.configuredRoutes.isEmpty()) {
            throw new IllegalArgumentException("at least one enabled LLM route is required");
        }
    }

    public ModelRoute selectInitial(Selection selection) {
        return eligibleRoutes(selection).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no LLM route satisfies the selection"));
    }

    public Optional<ModelRoute> selectFallback(
            Selection selection,
            Set<String> attemptedRouteKeys,
            FailureClass previousFailure) {
        Objects.requireNonNull(attemptedRouteKeys, "attemptedRouteKeys must not be null");
        Objects.requireNonNull(previousFailure, "previousFailure must not be null");

        if (!previousFailure.allowsFallback()) {
            return Optional.empty();
        }

        Set<String> attempted = Set.copyOf(attemptedRouteKeys);
        return eligibleRoutes(selection).stream()
                .filter(route -> !attempted.contains(route.key()))
                .findFirst();
    }

    private List<ModelRoute> eligibleRoutes(Selection selection) {
        Objects.requireNonNull(selection, "selection must not be null");

        Comparator<ModelRoute> preferenceOrder = Comparator
                .comparingInt((ModelRoute route) -> preferenceRank(route, selection))
                .thenComparingInt(ModelRoute::priority)
                .thenComparing(ModelRoute::provider)
                .thenComparing(ModelRoute::model);

        return configuredRoutes.stream()
                .filter(route -> route.capabilities().containsAll(selection.requiredCapabilities()))
                .sorted(preferenceOrder)
                .toList();
    }

    private static int preferenceRank(ModelRoute route, Selection selection) {
        boolean providerMatches = selection.preferredProvider().isBlank()
                || route.provider().equals(selection.preferredProvider());
        boolean modelMatches = selection.preferredModel().isBlank()
                || route.model().equals(selection.preferredModel());

        if (providerMatches && modelMatches) {
            return 0;
        }
        if (providerMatches) {
            return 1;
        }
        return 2;
    }

    public record ModelRoute(
            String provider,
            String model,
            int priority,
            boolean enabled,
            Set<String> capabilities) {

        public ModelRoute {
            provider = requireText(provider, "provider");
            model = requireText(model, "model");
            if (priority < 0) {
                throw new IllegalArgumentException("priority must not be negative");
            }
            capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
        }

        public String key() {
            return provider + "/" + model;
        }
    }

    public record Selection(
            String preferredProvider,
            String preferredModel,
            Set<String> requiredCapabilities) {

        public Selection {
            preferredProvider = preferredProvider == null ? "" : preferredProvider;
            preferredModel = preferredModel == null ? "" : preferredModel;
            requiredCapabilities = requiredCapabilities == null
                    ? Set.of()
                    : Set.copyOf(requiredCapabilities);
        }
    }

    public enum FailureClass {
        CAPACITY(true),
        PROVIDER_UNAVAILABLE(true),
        MODEL_UNAVAILABLE(true),
        RATE_LIMITED(true),
        TIMEOUT(false),
        INVALID_REQUEST(false),
        AUTHENTICATION(false),
        SAFETY(false),
        CANCELLED(false),
        UNKNOWN(false);

        private final boolean allowsFallback;

        FailureClass(boolean allowsFallback) {
            this.allowsFallback = allowsFallback;
        }

        public boolean allowsFallback() {
            return allowsFallback;
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
