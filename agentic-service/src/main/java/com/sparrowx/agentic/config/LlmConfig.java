package com.sparrowx.agentic.config;

import com.sparrowx.agentic.adapters.llm.LlmFallbackPolicy.ModelRoute;
import com.sparrowx.agentic.adapters.llm.StructuredLlmClient;
import com.sparrowx.agentic.adapters.llm.StructuredLlmClient.Request;
import com.sparrowx.agentic.adapters.llm.StructuredLlmResponse;
import com.sparrowx.agentic.components.IntentComponent;
import com.sparrowx.agentic.components.PlanningComponent;
import com.sparrowx.agentic.components.ReviewComponent;
import com.sparrowx.agentic.components.SynthesisComponent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configures structured-output model-provider routing.
 *
 * This layer selects a provider but does not perform a retry loop. Temporal
 * owns Activity retries, while LlmFallbackPolicy may select another configured
 * route within the same Activity attempt.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LlmConfig.LlmProperties.class)
public final class LlmConfig {

    @Bean
    @ConditionalOnMissingBean(StructuredLlmClient.class)
    public StructuredLlmClient structuredLlmClient(
            Map<String, StructuredLlmProvider> providers,
            LlmProperties properties
    ) {
        Map<String, StructuredLlmProvider> registered =
                Map.copyOf(providers);

        StructuredLlmClient.Backend backend =
                (route, request) -> invokeProvider(
                        registered,
                        properties,
                        route,
                        request
                );

        return new StructuredLlmClient(backend);
    }

    @Bean
    @ConditionalOnMissingBean(IntentComponent.Interpreter.class)
    public IntentComponent.Interpreter missingIntentInterpreter() {
        return request -> {
            throw missingReasoner(
                    "IntentComponent.Interpreter"
            );
        };
    }

    @Bean
    @ConditionalOnMissingBean(PlanningComponent.Planner.class)
    public PlanningComponent.Planner missingMissionPlanner() {
        return request -> {
            throw missingReasoner(
                    "PlanningComponent.Planner"
            );
        };
    }

    @Bean
    @ConditionalOnMissingBean(ReviewComponent.Reviewer.class)
    public ReviewComponent.Reviewer missingMissionReviewer() {
        return request -> {
            throw missingReasoner(
                    "ReviewComponent.Reviewer"
            );
        };
    }

    @Bean
    @ConditionalOnMissingBean(SynthesisComponent.Synthesizer.class)
    public SynthesisComponent.Synthesizer missingMissionSynthesizer() {
        return request -> {
            throw missingReasoner(
                    "SynthesisComponent.Synthesizer"
            );
        };
    }

    private static StructuredLlmResponse invokeProvider(
            Map<String, StructuredLlmProvider> providers,
            LlmProperties properties,
            ModelRoute route,
            Request request
    ) {
        Objects.requireNonNull(route, "route must not be null");
        Objects.requireNonNull(request, "request must not be null");

        String providerName = properties.providerFor(route.key());

        StructuredLlmProvider provider = providers.get(providerName);

        if (provider == null) {
            throw new IllegalStateException(
                    "No StructuredLlmProvider bean named '"
                            + providerName
                            + "' is configured for route '"
                            + route.key()
                            + "'"
            );
        }

        return Objects.requireNonNull(
                provider.complete(route, request),
                "StructuredLlmProvider returned null"
        );
    }

    private static IllegalStateException missingReasoner(
            String component
    ) {
        return new IllegalStateException(
                component
                        + " has no configured implementation. "
                        + "Register a provider-specific reasoning bean."
        );
    }

    @FunctionalInterface
    public interface StructuredLlmProvider {

        StructuredLlmResponse complete(
                ModelRoute route,
                Request request
        );
    }

    @ConfigurationProperties(
            prefix = "sparrowx.agentic.llm"
    )
    public static final class LlmProperties {

        private String defaultProvider = "";
        private Map<String, String> routeProviders =
                new LinkedHashMap<>();

        public String providerFor(String routeKey) {
            if (routeKey == null || routeKey.isBlank()) {
                throw new IllegalArgumentException(
                        "routeKey must not be blank"
                );
            }

            String provider = routeProviders.get(routeKey);

            if (provider == null || provider.isBlank()) {
                provider = defaultProvider;
            }

            if (provider == null || provider.isBlank()) {
                throw new IllegalStateException(
                        "No LLM provider is configured for route '"
                                + routeKey
                                + "'"
                );
            }

            return provider.trim();
        }

        public String getDefaultProvider() {
            return defaultProvider;
        }

        public void setDefaultProvider(String defaultProvider) {
            this.defaultProvider =
                    defaultProvider == null
                            ? ""
                            : defaultProvider.trim();
        }

        public Map<String, String> getRouteProviders() {
            return routeProviders;
        }

        public void setRouteProviders(
                Map<String, String> routeProviders
        ) {
            this.routeProviders =
                    routeProviders == null
                            ? new LinkedHashMap<>()
                            : new LinkedHashMap<>(routeProviders);
        }
    }
}