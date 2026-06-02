package com.sparrowx.document.observability;

import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.RetrievalMode;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RetrievalLifecycleLogger {

    private static final Logger log =
            LoggerFactory.getLogger(RetrievalLifecycleLogger.class);

    public void searchRequested(
            TenantId tenantId,
            UserId userId,
            ProjectId projectId,
            TeamId teamId,
            RetrievalMode mode,
            int limit,
            int scopedDocumentCount
    ) {
        log.info(
                "Document search requested tenantId={} userId={} projectId={} teamId={} mode={} limit={} scopedDocumentCount={}",
                value(tenantId),
                value(userId),
                value(projectId),
                value(teamId),
                mode,
                limit,
                scopedDocumentCount
        );
    }

    public void searchCompleted(
            TenantId tenantId,
            UserId userId,
            RetrievalMode mode,
            int evidenceCount
    ) {
        log.info(
                "Document search completed tenantId={} userId={} mode={} evidenceCount={}",
                value(tenantId),
                value(userId),
                mode,
                evidenceCount
        );
    }

    public void searchFailed(
            TenantId tenantId,
            UserId userId,
            RetrievalMode mode,
            String reason
    ) {
        log.error(
                "Document search failed tenantId={} userId={} mode={} reason={}",
                value(tenantId),
                value(userId),
                mode,
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