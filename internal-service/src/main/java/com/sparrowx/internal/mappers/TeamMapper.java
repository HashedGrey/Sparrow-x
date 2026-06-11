package com.sparrowx.internal.mappers;

import com.sparrowx.internal.features.team.createteam.CreateTeamCommand;
import com.sparrowx.internal.features.team.createteam.CreateTeamResult;
import com.sparrowx.internal.features.team.getteam.GetTeamQuery;
import com.sparrowx.internal.features.team.getteam.GetTeamResult;
import com.sparrowx.internal.grpc.CreateTeamRequest;
import com.sparrowx.internal.grpc.CreateTeamResponse;
import com.sparrowx.internal.grpc.GetTeamRequest;
import com.sparrowx.internal.grpc.GetTeamResponse;
import com.sparrowx.internal.grpc.Team;

public final class TeamMapper {

    private TeamMapper() {
    }

    public static CreateTeamCommand toCreateTeamCommand(
            CreateTeamRequest request
    ) {
        return new CreateTeamCommand(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getName(),
                request.getDescription()
        );
    }

    public static CreateTeamResponse toCreateTeamResponse(
            CreateTeamResult result
    ) {
        return CreateTeamResponse.newBuilder()
                .setTeam(toProto(result.team()))
                .build();
    }

    public static GetTeamQuery toGetTeamQuery(
            GetTeamRequest request
    ) {
        return new GetTeamQuery(
                InternalMapper.tenantId(request.getContext()),
                InternalMapper.actorId(request.getContext()),
                InternalMapper.requestId(request.getContext()),
                request.getTeamId()
        );
    }

    public static GetTeamResponse toGetTeamResponse(
            GetTeamResult result
    ) {
        return GetTeamResponse.newBuilder()
                .setTeam(toProto(result.team()))
                .build();
    }

    public static Team toProto(
            com.sparrowx.internal.models.Team team
    ) {
        return Team.newBuilder()
                .setTeamId(InternalMapper.value(team.teamId()))
                .setTenantId(InternalMapper.value(team.tenantId()))
                .setName(team.name())
                .setSlug(team.slug())
                .setDescription(team.description())
                .setCreatedAt(InternalMapper.toTimestamp(team.createdAt()))
                .setUpdatedAt(InternalMapper.toTimestamp(team.updatedAt()))
                .build();
    }
}