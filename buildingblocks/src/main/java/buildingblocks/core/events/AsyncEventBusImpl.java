package buildingblocks.core.events;

import buildingblocks.infrastructure.messaging.internal.AsyncCommandExecutor;
import org.springframework.stereotype.Component;

@Component
public class AsyncEventBusImpl implements AsyncEventBus {

    private final AsyncCommandExecutor executor;
    private final EventBus eventBus;

    public AsyncEventBusImpl(
            AsyncCommandExecutor executor,
            EventBus eventBus
    ) {
        this.executor = executor;
        this.eventBus = eventBus;
    }

    @Override
    public void publishAsync(DomainEvent event) {
        executor.submit(() -> {
            eventBus.publish(event);
            return null;
        });
    }
}