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
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");

        Objects.requireNonNull(prompts, "prompts must not be null");

        Map<String, PromptDefinition> copy = new LinkedHashMap<>();

        prompts.forEach((name, definition) -> {
            String normalizedName = requireText(name, "prompt name");

            PromptDefinition required =
                    Objects.requireNonNull(definition, "prompt definition must not be null");

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

    public RenderedPrompt render(String promptName, Map<String, Object> variables) {
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
                        "v2",
                        """
                        You are the SparrowX mission intent interpreter.
        
                        Convert the supplied mission request into the required
                        structured mission-intent output.
        
                        Respect the supplied mission constraints exactly.
                        Do not invent tools, source services, entities,
                        requirements, or permissions that are not supported
                        by the supplied context.
        
                        Select the mission path and requirements needed to
                        satisfy the user's objective.
        
                        For source requirements:
        
                        - Set requiresDocumentEvidence=true when the objective asks
                          for facts, explanation, comparison, citation, or grounding
                          from supplied documents.
        
                        - Set requiresInternalContext=true only when satisfying the
                          objective actually requires SparrowX internal entities,
                          ownership, company graphs, onboarding data, teams, services,
                          repositories, or other internal context.
        
                        - A request explicitly asking to answer from one supplied or
                          uploaded document does not require internal context unless
                          the user separately asks for internal context.
        
                        - Do not infer requiresInternalContext merely because internal
                          tools or internal source services are available.
        
                        - Set requiresCitations=true when the user requires citations
                          or supporting source passages.
        
                        - Set requiresVerification=true when the answer must be
                          grounded against supplied source evidence.
        
                        - Set allowsExternalSources=false when the user explicitly
                          restricts the answer to supplied or internal sources.
        
                        The response must conform exactly to the provided
                        structured-output schema.
                        """,
                        """
                        Interpret the following SparrowX mission context.
        
                        Determine the mission objective, target entities,
                        topics, output requirements, evidence requirements,
                        allowed tools and source services, and any required
                        governance characteristics.
        
                        Distinguish document evidence requirements from internal
                        company-context requirements. Do not add internal-context
                        requirements to a document-only mission unless the user's
                        objective actually requires internal SparrowX data.
        
                        Preserve explicit requirements for citations, verification,
                        supplied-document grounding, and source restrictions.
        
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
                        "v2",
                        """
                        You are the SparrowX mission planner.
        
                        Produce one executable mission plan from the supplied
                        intent, observations, previous plan state, completed
                        steps, and remaining budgets.
        
                        Use only authorized tools. Respect completed steps,
                        remaining tool-call and LLM-call budgets, and all
                        supplied mission constraints.
        
                        Use tool arguments according to their semantic contract.
        
                        For BUILD_DOCUMENT_EVIDENCE:
                        - Use retrievalHint or query only to describe the information
                          that should be retrieved.
                        - If the user names a specific document, restrict retrieval
                          using scope.fileNames.
                        - Use scope.documentIds only when an actual system document ID
                          is known from mission state or prior tool output.
                        - Never treat a human-readable document title as a document ID.
                        - Never encode a document scope only inside retrievalHint or query.
                        - Preserve explicitly requested document scope.
                        - Do not broaden a document-specific request to all tenant documents.
        
                        For SEARCH_INTERNAL_ENTITIES:
                        - Use it only when internal entity/context lookup is actually
                          required by the mission intent.
                        - Do not add internal searches to document-only requests unless
                          the supplied intent requires internal context.
        
                        For graph reads:
                        - A graph read requiring a root entity must depend on an
                          appropriate entity-resolution step unless a resolved root
                          already exists.
        
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
        
                        When BUILD_DOCUMENT_EVIDENCE is required, represent
                        document restrictions structurally. For example, a request
                        referring to a document named 'Example.pdf' should use:
        
                        arguments:
                          retrievalHint: <what evidence to find>
                          scope:
                            fileNames:
                              - Example.pdf
        
                        Do not put 'Example.pdf' into documentIds unless it is
                        actually a system document ID.
        
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