package com.sparrowx.agentic.mission.artifact;

import java.util.List;
import java.util.Map;

/**
 * Prepared references, verified hashes, downstream upload outcomes and warnings.
 */
public record ArtifactPreparationResult(
        List<PreparedArtifact> preparedArtifacts,
        Map<String, String> artifactHashes,
        Map<String, String> uploadOutcomes,
        List<String> warnings
) {

    public ArtifactPreparationResult {
        preparedArtifacts = preparedArtifacts == null
                ? List.of()
                : List.copyOf(preparedArtifacts);
        artifactHashes = artifactHashes == null ? Map.of() : Map.copyOf(artifactHashes);
        uploadOutcomes = uploadOutcomes == null ? Map.of() : Map.copyOf(uploadOutcomes);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}