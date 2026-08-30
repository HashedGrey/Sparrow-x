package com.sparrowx.agentic.prompts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Versioned prompt registry and deterministic renderer.
 *
 * PromptPack owns prompt text and versioning only.
 * Structured-output schemas and model routing remain external concerns.
 */
@Component
public final class PromptPack {

    public static final String INTENT_PROMPT = "intent-prompt";
    public static final String PLANNING_PROMPT = "planning-prompt";

    private final ObjectMapper objectMapper;
    private final Map<String, PromptDefinition> prompts;

    @Autowired
    public PromptPack(ObjectMapper objectMapper) {
        this(objectMapper, defaultPrompts());
    }

    public PromptPack(
            ObjectMapper objectMapper,
            Map<String, PromptDefinition> prompts
    ) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );

        Objects.requireNonNull(
                prompts,
                "prompts must not be null"
        );

        Map<String, PromptDefinition> copy =
                new LinkedHashMap<>();

        prompts.forEach((name, definition) -> {
            String normalizedName =
                    requireText(name, "prompt name");

            PromptDefinition required =
                    Objects.requireNonNull(
                            definition,
                            "prompt definition must not be null"
                    );

            if (!normalizedName.equals(required.name())) {
                throw new IllegalArgumentException(
                        "prompt registry key does not match "
                                + "definition name: "
                                + normalizedName
                );
            }

            copy.put(normalizedName, required);
        });

        this.prompts = Map.copyOf(copy);
    }

    public RenderedPrompt render(
            String promptName,
            Map<String, Object> variables
    ) {
        String name = requireText(
                promptName,
                "promptName"
        );

        PromptDefinition definition = prompts.get(name);

        if (definition == null) {
            throw new IllegalArgumentException(
                    "unknown prompt: " + name
            );
        }

        Map<String, Object> safeVariables =
                variables == null
                        ? Map.of()
                        : new LinkedHashMap<>(variables);

        String renderedVariables =
                serializeVariables(safeVariables);

        String userPrompt =
                definition.userInstruction()
                        + "\n\n"
                        + "Mission context:\n"
                        + renderedVariables;

        Map<String, String> metadata =
                new LinkedHashMap<>(
                        definition.metadata()
                );

        metadata.put(
                "prompt_name",
                definition.name()
        );
        metadata.put(
                "prompt_version",
                definition.version()
        );

        return new RenderedPrompt(
                definition.name(),
                definition.version(),
                definition.systemPrompt(),
                userPrompt,
                metadata
        );
    }

    public PromptDefinition definition(
            String promptName
    ) {
        String name = requireText(
                promptName,
                "promptName"
        );

        PromptDefinition definition =
                prompts.get(name);

        if (definition == null) {
            throw new IllegalArgumentException(
                    "unknown prompt: " + name
            );
        }

        return definition;
    }

    public String version(String promptName) {
        return definition(promptName).version();
    }

    private String serializeVariables(
            Map<String, Object> variables
    ) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(variables);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    "unable to render prompt variables",
                    exception
            );
        }
    }

    private static Map<String, PromptDefinition>
    defaultPrompts() {

        Map<String, PromptDefinition> prompts =
                new LinkedHashMap<>();

        prompts.put(
                INTENT_PROMPT,
                new PromptDefinition(
                        INTENT_PROMPT,
                        "v1",
                        """
                        You are the SparrowX mission intent interpreter.

                        Convert the supplied mission request into the
                        required structured mission-intent output.

                        Respect the supplied mission constraints exactly.
                        Do not invent tools, source services, entities,
                        requirements, or permissions that are not supported
                        by the supplied context.

                        Select the mission path and requirements needed to
                        satisfy the user's objective. The response must
                        conform exactly to the provided structured-output
                        schema.
                        """,
                        """
                        Interpret the following SparrowX mission context.

                        Determine the mission objective, target entities,
                        topics, output requirements, evidence requirements,
                        allowed tools and source services, and any required
                        governance characteristics.

                        Return only data conforming to the requested
                        structured-output schema.
                        """,
                        Map.of(
                                "component", "intent",
                                "task", "mission-intent"
                        )
                )
        );

        prompts.put(
                PLANNING_PROMPT,
                new PromptDefinition(
                        PLANNING_PROMPT,
                        "v1",
                        """
                        You are the SparrowX mission planner.

                        Produce one executable mission plan from the supplied
                        intent, observations, previous plan state, completed
                        steps, and remaining budgets.

                        Use only authorized tools. Respect completed steps,
                        remaining tool-call and LLM-call budgets, and all
                        supplied mission constraints.

                        Do not invent observations or claim that work has
                        already occurred. The response must conform exactly
                        to the provided structured-output schema.
                        """,
                        """
                        Plan the next SparrowX mission execution state.

                        Use the supplied mission intent, current plan when
                        present, prior observations, completed steps,
                        authorized tools, and remaining budgets.

                        Produce a coherent ordered plan containing only work
                        still necessary to satisfy the mission.

                        Return only data conforming to the requested
                        structured-output schema.
                        """,
                        Map.of(
                                "component", "planning",
                                "task", "mission-plan"
                        )
                )
        );

        return Map.copyOf(prompts);
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }

    public record PromptDefinition(
            String name,
            String version,
            String systemPrompt,
            String userInstruction,
            Map<String, String> metadata
    ) {

        public PromptDefinition {
            name = requireText(name, "name");
            version = requireText(version, "version");
            systemPrompt = requireText(
                    systemPrompt,
                    "systemPrompt"
            );
            userInstruction = requireText(
                    userInstruction,
                    "userInstruction"
            );
            metadata = metadata == null
                    ? Map.of()
                    : Map.copyOf(metadata);
        }
    }

    public record RenderedPrompt(
            String name,
            String version,
            String systemPrompt,
            String userPrompt,
            Map<String, String> metadata
    ) {

        public RenderedPrompt {
            name = requireText(name, "name");
            version = requireText(
                    version,
                    "version"
            );
            systemPrompt = requireText(
                    systemPrompt,
                    "systemPrompt"
            );
            userPrompt = requireText(
                    userPrompt,
                    "userPrompt"
            );
            metadata = metadata == null
                    ? Map.of()
                    : Map.copyOf(metadata);
        }
    }
}