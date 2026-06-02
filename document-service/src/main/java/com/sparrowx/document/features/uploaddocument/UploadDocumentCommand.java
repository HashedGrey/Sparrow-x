package com.sparrowx.document.features.uploaddocument;

import buildingblocks.core.commands.Command;
import com.sparrowx.document.domain.valueobjects.CallerService;
import com.sparrowx.document.domain.valueobjects.DocumentTitle;
import com.sparrowx.document.domain.valueobjects.FileName;
import com.sparrowx.document.domain.valueobjects.MimeType;
import com.sparrowx.document.domain.valueobjects.ProjectId;
import com.sparrowx.document.domain.valueobjects.RequestId;
import com.sparrowx.document.domain.valueobjects.TeamId;
import com.sparrowx.document.domain.valueobjects.TenantId;
import com.sparrowx.document.domain.valueobjects.TraceId;
import com.sparrowx.document.domain.valueobjects.UserId;

public record UploadDocumentCommand(
        RequestId requestId,
        TenantId tenantId,
        UserId userId,
        ProjectId projectId,
        TeamId teamId,
        TraceId traceId,
        CallerService callerService,
        FileName fileName,
        MimeType mimeType,
        byte[] content,
        DocumentTitle title
) implements Command<UploadDocumentResult> {
}