package buildingblocks.core.events;

public interface AsyncEventBus {
    void publishAsync(DomainEvent event);
}