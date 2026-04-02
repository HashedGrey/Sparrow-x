package buildingblocks.core.events;

public interface EventBus {
    void publish(DomainEvent event);
}