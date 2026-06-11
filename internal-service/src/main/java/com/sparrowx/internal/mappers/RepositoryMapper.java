package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.repository.createrepository.CreateRepositoryCommand;
import com.sparrowx.internal.features.repository.createrepository.CreateRepositoryResult;
import com.sparrowx.internal.features.repository.getrepository.GetRepositoryQuery;
import com.sparrowx.internal.features.repository.getrepository.GetRepositoryResult;
import com.sparrowx.internal.grpc.CreateRepositoryRequest;
import com.sparrowx.internal.grpc.CreateRepositoryResponse;
import com.sparrowx.internal.grpc.GetRepositoryRequest;
import com.sparrowx.internal.grpc.GetRepositoryResponse;
import com.sparrowx.internal.grpc.Repository;

public final class RepositoryMapper {

    private RepositoryMapper() {
    }

    public static CreateRepositoryCommand toCreateRepositoryCommand(
            CreateRepositoryRequest request
    ) {
        return new CreateRepositoryCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getName(),
                request.getUrl(),
                request.getModuleId()
        );
    }

    public static CreateRepositoryResponse toCreateRepositoryResponse(
            CreateRepositoryResult result
    ) {
        return CreateRepositoryResponse.newBuilder()
                .setRepository(toProto(result.repository()))
                .build();
    }

    public static GetRepositoryQuery toGetRepositoryQuery(
            GetRepositoryRequest request
    ) {
        return new GetRepositoryQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getRepositoryId()
        );
    }

    public static GetRepositoryResponse toGetRepositoryResponse(
            GetRepositoryResult result
    ) {
        return GetRepositoryResponse.newBuilder()
                .setRepository(toProto(result.repository()))
                .build();
    }

    public static Repository toProto(
            com.sparrowx.internal.models.Repository repository
    ) {
        return Repository.newBuilder()
                .setRepositoryId(InternalMapper.value(repository.repositoryId()))
                .setTenantId(InternalMapper.value(repository.tenantId()))
                .setName(repository.name())
                .setUrl(repository.url())
                .setModuleId(InternalMapper.value(repository.moduleId()))
                .setCreatedAt(InternalMapper.toTimestamp(repository.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(repository.updatedAt()))
                .build();
    }
}