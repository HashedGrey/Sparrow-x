package com.sparrowx.agentic.features.getmissionresult;

import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.agentic.exceptions.AgenticServiceException;
import com.sparrowx.agentic.mission.MissionResultReadService;
import org.springframework.stereotype.Component;

@Component
public final class GetMissionResultQueryHandler
        implements QueryHandler<GetMissionResultQuery, GetMissionResultView> {

    private final GetMissionResultQueryValidator validator;
    private final MissionResultReadService resultReadService;

    public GetMissionResultQueryHandler(
            GetMissionResultQueryValidator validator,
            MissionResultReadService resultReadService
    ) {
        this.validator = validator;
        this.resultReadService = resultReadService;
    }

    @Override
    public GetMissionResultView handle(GetMissionResultQuery query) {
        validator.validate(query);

        GetMissionResultView view = resultReadService.read(
                query.tenantId(),
                query.missionId()
        );

        if (view == null) {
            throw new AgenticServiceException(
                    "Mission result service returned no result view."
            );
        }

        if (!query.missionId().equals(view.missionId())) {
            throw new AgenticServiceException(
                    "Mission result belongs to a different mission."
            );
        }

        return view;
    }
}