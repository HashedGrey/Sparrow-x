package com.sparrowx.profile.data.cassandra.repositories;


import com.sparrowx.profile.data.cassandra.tables.ProfileTable;
import com.sparrowx.profile.valueobjects.ProfileId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileCassandraRepository extends CassandraRepository<ProfileTable, ProfileId> {

    Optional<ProfileTable> findByUsername(String username);

    Optional<ProfileTable> findByEmail(String email);

}
