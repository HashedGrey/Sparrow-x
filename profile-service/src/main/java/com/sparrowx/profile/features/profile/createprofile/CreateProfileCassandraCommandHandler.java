package com.sparrowx.profile.features.profile.createprofile;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import buildingblocks.mediator.abstractions.requests.Unit;
import com.sparrowx.profile.data.cassandra.tables.ProfileTable;
import com.sparrowx.profile.data.cassandra.repositories.ProfileCassandraRepository;
import com.sparrowx.profile.mappers.ProfileMapper;
import org.springframework.stereotype.Service;

@Service
public class CreateProfileCassandraCommandHandler implements ICommandHandler<CreateProfileCassandraCommand, Unit> {

    private final ProfileCassandraRepository profileCassandraRepository;

    public CreateProfileCassandraCommandHandler(ProfileCassandraRepository profileCassandraRepository) {
        this.profileCassandraRepository = profileCassandraRepository;
    }

    @Override
    public Unit handle(CreateProfileCassandraCommand command) {
        ProfileTable profileRow = ProfileMapper.toProfileTable(command);

        profileCassandraRepository.save(profileRow);
        return Unit.VALUE;
    }
}
