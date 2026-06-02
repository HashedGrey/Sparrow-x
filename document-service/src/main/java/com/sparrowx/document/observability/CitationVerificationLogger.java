package com.sparrowx.document.observability;

import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CitationVerificationLogger {

    private static final Logger log =
            LoggerFactory.getLogger(CitationVerificationLogger.class);

    public void verificationRequested(
            TenantId tenantId,
            UserId userId,
            int evidenceCount
    ) {
        log.info(
                "Citation verification requested tenantId={} userId={} evidenceCount={}",
                value(tenantId),
                value(userId),
                evidenceCount
        );
    }

    public void verificationCompleted(
            TenantId tenantId,
            UserId userId,
            boolean supported,
            double confidence
    ) {
        log.info(
                "Citation verification completed tenantId={} userId={} supported={} confidence={}",
                value(tenantId),
                value(userId),
                supported,
                confidence
        );
    }

    public void verificationFailed(
            TenantId tenantId,
            UserId userId,
            String reason
    ) {
        log.error(
                "Citation verification failed tenantId={} userId={} reason={}",
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