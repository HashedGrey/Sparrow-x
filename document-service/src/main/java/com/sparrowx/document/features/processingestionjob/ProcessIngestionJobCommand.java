package com.sparrowx.document.features.processingestionjob;

import buildingblocks.core.commands.NonTransactionalCommand;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.IngestionJobId;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ObjectKey;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.DocumentId;

public record ProcessIngestionJobCommand(
        IngestionJobId ingestionJobId,
        DocumentId documentId,
        TenantId tenantId,
        ProjectId projectId,
        TeamId teamId,
        ObjectKey objectKey,
        FileName fileName,
        MimeType mimeType
) implements NonTransactionalCommand<ProcessIngestionJobResult> {
}