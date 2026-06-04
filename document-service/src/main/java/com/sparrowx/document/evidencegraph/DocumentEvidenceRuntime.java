package com.sparrowx.document.evidencegraph;

import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceResult;
import com.sparrowx.document.verification.EvidenceGraphVerifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentEvidenceRuntime {

    private final EvidenceBuildOrchestrator evidenceBuildOrchestrator;
    private final EvidenceGraphVerifier evidenceGraphVerifier;
    private final EvidenceGraphPolicy evidenceGraphPolicy;
    private final EvidenceGraphResponseCompactor responseCompactor;

    public DocumentEvidenceRuntime(
            EvidenceBuildOrchestrator evidenceBuildOrchestrator,
            EvidenceGraphVerifier evidenceGraphVerifier,
            EvidenceGraphPolicy evidenceGraphPolicy,
            EvidenceGraphResponseCompactor responseCompactor
    ) {
        this.evidenceBuildOrchestrator = evidenceBuildOrchestrator;
        this.evidenceGraphVerifier = evidenceGraphVerifier;
        this.evidenceGraphPolicy = evidenceGraphPolicy;
        this.responseCompactor = responseCompactor;
    }

    public BuildDocumentEvidenceResult build(BuildDocumentEvidenceCommand command) {
        EvidenceBuildOrchestrator.EvidenceBuildOrchestrationResult orchestrationResult =
                evidenceBuildOrchestrator.build(command);

        DocumentEvidenceGraph graph = orchestrationResult.graph();
        List<String> warnings = new ArrayList<>(orchestrationResult.warnings());

        if (command.requireVerification() && graph != null && !graph.nodes().isEmpty()) {
            EvidenceGraphVerifier.EvidenceGraphVerificationResult verificationResult =
                    evidenceGraphVerifier.verify(
                            graph,
                            true,
                            false
                    );

            graph = verificationResult.verifiedGraph();
            warnings.addAll(verificationResult.warnings());
            warnings.add(verificationResult.explanation());
        }

        EvidenceGraphPolicy.PolicyResult policyResult =
                evidenceGraphPolicy.evaluate(command, graph);

        warnings.addAll(policyResult.warnings());

        boolean contradicted =
                graph != null && graph.verificationStatus() == VerificationStatus.CONTRADICTED;

        if (!policyResult.acceptable() && !contradicted) {
            graph = downgradeGraphForPolicyFailure(graph, warnings);
        }

        if (!policyResult.acceptable() && contradicted) {
            warnings.add("Evidence graph policy was not acceptable, but CONTRADICTED status was preserved.");
        }

        graph = responseCompactor.compact(graph);

        return new BuildDocumentEvidenceResult(
                graph,
                orchestrationResult.usedChunkRetrieval(),
                orchestrationResult.usedClaimCache(),
                graph == null ? 0.0 : graph.coverageScore(),
                compactWarnings(warnings)
        );
    }

    private DocumentEvidenceGraph downgradeGraphForPolicyFailure(
            DocumentEvidenceGraph graph,
            List<String> warnings
    ) {
        if (graph == null) {
            return null;
        }

        List<String> graphWarnings = new ArrayList<>(graph.warnings());
        graphWarnings.addAll(warnings);
        graphWarnings.add("Evidence graph failed policy evaluation.");

        VerificationStatus status = graph.nodes().isEmpty()
                ? VerificationStatus.UNSUPPORTED
                : VerificationStatus.NEEDS_SOURCE_CONTEXT;

        return new DocumentEvidenceGraph(
                graph.graphId(),
                graph.goal(),
                graph.customGoal(),
                graph.nodes(),
                graph.edges(),
                graph.sourcePool(),
                status,
                Math.min(graph.confidence(), 0.25),
                0.0,
                compactWarnings(graphWarnings),
                graph.missingNodeTypes(),
                graph.outputSchemaRef(),
                graph.outputSchemaVersion(),
                graph.createdAt()
        );
    }

    private List<String> compactWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return List.of();
        }

        return warnings.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(12)
                .toList();
    }
}