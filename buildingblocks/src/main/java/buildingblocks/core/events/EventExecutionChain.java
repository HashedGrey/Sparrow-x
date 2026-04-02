package buildingblocks.core.events;

@FunctionalInterface
public interface EventExecutionChain<T extends DomainEvent> {
    void proceed(T event);
}