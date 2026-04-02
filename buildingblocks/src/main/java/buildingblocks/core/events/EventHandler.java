package buildingblocks.core.events;

public interface EventHandler<E extends DomainEvent> {
    void handle(E event);
}