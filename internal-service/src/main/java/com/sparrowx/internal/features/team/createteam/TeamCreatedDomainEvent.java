package com.sparrowx.internal.features.team.createteam;

import buildingblocks.core.events.DomainEvent;
import lombok.Getter;

@Getter
public class TeamCreatedDomainEvent extends DomainEvent {

    private final String tenantId;
    private final String teamId;
    private final String name;
    private final String slug;
    private final String actorId;
    private final String requestId;

    public TeamCreatedDomainEvent(
            String tenantId,
            String teamId,
            String name,
            String slug,
            String actorId,
            String requestId
    ) {
        super(teamId);
        this.tenantId = tenantId;
        this.teamId = teamId;
        this.name = name;
        this.slug = slug;
        this.actorId = actorId;
        this.requestId = requestId;
    }

}