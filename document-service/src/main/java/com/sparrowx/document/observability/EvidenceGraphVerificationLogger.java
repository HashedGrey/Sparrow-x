package com.sparrowx.document.observability;

import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.UserId;
import com.sparrowx.document.domain.valueobjects.VerificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EvidenceGraphVerificationLogger {

    private static final Logger log =
            LoggerFactory.getLogger(EvidenceGraphVerificationLogger.class);

    public void verificationRequested(
            TenantId tenantId,
            UserId userId,
            int nodeCount,
            int edgeCount
    ) {
        log.info(
                "Evidence graph verification requested tenantId={} userId={} nodeCount={} edgeCount={}",
                value(tenantId),
                value(userId),
                nodeCount,
                edgeCount
        );
    }

    public void verificationCompleted(
            TenantId tenantId,
            UserId userId,
            boolean supported,
            VerificationStatus verificationStatus,
            double confidence,
            double coverageScore
    ) {
        log.info(
                "Evidence graph verification completed tenantId={} userId={} supported={} verificationStatus={} confidence={} coverageScore={}",
                value(tenantId),
                value(userId),
                supported,
                verificationStatus,
                confidence,
                coverageScore
        );
    }

    public void verificationFailed(
            TenantId tenantId,
            UserId userId,
            String reason
    ) {
        log.error(
                "Evidence graph verification failed tenantId={} userId={} reason={}",
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
}