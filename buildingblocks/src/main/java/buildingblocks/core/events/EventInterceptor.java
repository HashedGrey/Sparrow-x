package buildingblocks.core.events;

public interface EventInterceptor<E extends DomainEvent> {

    void intercept(
            E event,
            EventExecutionChain chain
    );
}