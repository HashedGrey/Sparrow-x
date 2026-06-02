package com.sparrowx.document.features.verifyevidencegraph;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.document.exceptions.CitationVerificationException;
import com.sparrowx.document.exceptions.InvalidDocumentException;
import com.sparrowx.document.observability.EvidenceGraphVerificationLogger;
import com.sparrowx.document.verification.EvidenceGraphVerifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VerifyEvidenceGraphQueryHandler
        implements QueryHandler<VerifyEvidenceGraphQuery, VerifyEvidenceGraphResult> {

    private final EvidenceGraphVerifier evidenceGraphVerifier;
    private final EvidenceGraphVerificationLogger verificationLogger;

    public VerifyEvidenceGraphQueryHandler(
            EvidenceGraphVerifier evidenceGraphVerifier,
            EvidenceGraphVerificationLogger verificationLogger
    ) {
        this.evidenceGraphVerifier = evidenceGraphVerifier;
        this.verificationLogger = verificationLogger;
    }

    @Override
    @Transactional(readOnly = true)
    public VerifyEvidenceGraphResult handle(VerifyEvidenceGraphQuery query) {
        validate(query);

        verificationLogger.verificationRequested(
                query.tenantId(),
                query.userId(),
                query.graph().nodes().size(),
                query.graph().edges().size()
        );

        try {
            EvidenceGraphVerifier.EvidenceGraphVerificationResult result =
                    evidenceGraphVerifier.verify(
                            query.graph(),
                            query.requireAllNodesSupported(),
                            query.requireAllEdgesSupported()
                    );

            verificationLogger.verificationCompleted(
                    query.tenantId(),
                    query.userId(),
                    result.supported(),
                    result.verificationStatus(),
                    result.confidence(),
                    result.coverageScore()
            );

            return new VerifyEvidenceGraphResult(
                    result.supported(),
                    result.verificationStatus(),
                    result.confidence(),
                    result.coverageScore(),
                    result.verifiedGraph(),
                    result.unsupportedNodeIds(),
                    result.unsupportedEdgeIds(),
                    result.warnings(),
                    result.explanation()
            );

        } catch (RuntimeException exception) {
            verificationLogger.verificationFailed(
                    query.tenantId(),
                    query.userId(),
                    exception.getMessage()
            );

            if (exception instanceof CitationVerificationException) {
                throw exception;
            }

            throw new CitationVerificationException(
                    "Evidence graph verification failed",
                    exception
            );
        }
    }

    private void validate(VerifyEvidenceGraphQuery query) {
        if (query == null) {
            throw InvalidDocumentException.nullQuery("VerifyEvidenceGraphQuery");
        }

        if (query.tenantId() == null) {
            throw InvalidDocumentException.blankField("tenantId");
        }

        if (query.userId() == null) {
            throw InvalidDocumentException.blankField("userId");
        }

        if (query.graph() == null) {
            throw InvalidDocumentException.blankField("graph");
        }

        if (query.graph().nodes() == null || query.graph().nodes().isEmpty()) {
            throw new CitationVerificationException("graph nodes must not be empty");
        }
    }
}