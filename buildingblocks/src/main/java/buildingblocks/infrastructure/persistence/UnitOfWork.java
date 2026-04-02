package buildingblocks.infrastructure.persistence;

import buildingblocks.domain.model.AggregateRoot;

import java.util.function.Supplier;

public interface UnitOfWork {

    <T> T execute(Supplier<T> action);

    void registerAggregate(AggregateRoot<?> aggregate);
}