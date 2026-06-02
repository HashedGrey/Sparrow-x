package com.sparrowx.document.observability;

import com.sparrowx.document.domain.models.DocumentEvidenceGraph;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EvidenceBuildLogger {

    private static final Logger log =
            LoggerFactory.getLogger(EvidenceBuildLogger.class);

    public void buildRequested(
            TenantId tenantId,
            UserId userId,
            ProjectId projectId,
            TeamId teamId,
            DocumentEvidenceGraph.EvidenceGoal goal,
            int scopedDocumentCount,
            int limit,
            boolean allowClaimCache,
            boolean requireVerification
    ) {
        log.info(
                "Document evidence build requested tenantId={} userId={} projectId={} teamId={} goal={} scopedDocumentCount={} limit={} allowClaimCache={} requireVerification={}",
                value(tenantId),
                value(userId),
                value(projectId),
                value(teamId),
                goal,
                scopedDocumentCount,
                limit,
                allowClaimCache,
                requireVerification
        );
    }

    public void buildCompleted(
            TenantId tenantId,
            UserId userId,
            String graphId,
            int nodeCount,
            int edgeCount,
            boolean usedChunkRetrieval,
            boolean usedClaimCache,
            double coverageScore
    ) {
        log.info(
                "Document evidence build completed tenantId={} userId={} graphId={} nodeCount={} edgeCount={} usedChunkRetrieval={} usedClaimCache={} coverageScore={}",
                value(tenantId),
                value(userId),
                graphId,
                nodeCount,
                edgeCount,
                usedChunkRetrieval,
                usedClaimCache,
                coverageScore
        );
    }

    public void buildFailed(
            TenantId tenantId,
            UserId userId,
            String reason
    ) {
        log.error(
                "Document evidence build failed tenantId={} userId={} reason={}",
                value(tenantId),
                value(userId),
                reason
        );
    }

    private String value(TenantId tenantId) {
        return tenantId == null ? null : tenantId.value();
    }

    private String value(UserId userId) {
        return userId == null ? null : userId.value();
    }

    private String value(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }

    private String value(TeamId teamId) {
        return teamId == null ? null : teamId.value();
    }
}