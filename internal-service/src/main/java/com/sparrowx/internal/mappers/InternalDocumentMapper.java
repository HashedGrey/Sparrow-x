package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.document.createdocument.CreateDocumentCommand;
import com.sparrowx.internal.features.document.createdocument.CreateDocumentResult;
import com.sparrowx.internal.features.document.getdocument.GetDocumentQuery;
import com.sparrowx.internal.features.document.getdocument.GetDocumentResult;
import com.sparrowx.internal.grpc.CreateInternalDocumentRequest;
import com.sparrowx.internal.grpc.CreateInternalDocumentResponse;
import com.sparrowx.internal.grpc.GetInternalDocumentRequest;
import com.sparrowx.internal.grpc.GetInternalDocumentResponse;
import com.sparrowx.internal.grpc.InternalDocument;

public final class InternalDocumentMapper {

    private InternalDocumentMapper() {
    }

    public static CreateDocumentCommand toCreateInternalDocumentCommand(
            CreateInternalDocumentRequest request
    ) {
        return new CreateDocumentCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getTitle(),
                request.getSummary(),
                request.getModuleId(),
                request.getRepositoryId(),
                request.getExternalRef()
        );
    }

    public static CreateInternalDocumentResponse toCreateInternalDocumentResponse(
            CreateDocumentResult result
    ) {
        return CreateInternalDocumentResponse.newBuilder()
                .setDocument(toProto(result.document()))
                .build();
    }

    public static GetDocumentQuery toGetInternalDocumentQuery(
            GetInternalDocumentRequest request
    ) {
        return new GetDocumentQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getDocumentId()
        );
    }

    public static GetInternalDocumentResponse toGetInternalDocumentResponse(
            GetDocumentResult result
    ) {
        return GetInternalDocumentResponse.newBuilder()
                .setDocument(toProto(result.document()))
                .build();
    }

    public static InternalDocument toProto(
            com.sparrowx.internal.models.InternalDocument document
    ) {
        return InternalDocument.newBuilder()
                .setDocumentId(InternalMapper.value(document.documentId()))
                .setTenantId(InternalMapper.value(document.tenantId()))
                .setTitle(document.title())
                .setSlug(document.slug())
                .setSummary(document.summary())
                .setModuleId(InternalMapper.value(document.moduleId()))
                .setRepositoryId(InternalMapper.value(document.repositoryId()))
                .setExternalRef(document.externalRef())
                .setCreatedAt(InternalMapper.toTimestamp(document.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(document.updatedAt()))
                .build();
    }
}