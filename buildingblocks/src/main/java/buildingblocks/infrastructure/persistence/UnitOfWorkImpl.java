package buildingblocks.infrastructure.persistence;

import buildingblocks.core.events.DomainEvent;
import buildingblocks.core.events.EventBus;
import buildingblocks.domain.model.AggregateRoot;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Component
public class UnitOfWorkImpl implements UnitOfWork {

    private final TransactionTemplate transactionTemplate;
    private final EventBus eventBus;

    // Track aggregates participating in current transaction
    private final ThreadLocal<Set<AggregateRoot<?>>> trackedAggregates =
            ThreadLocal.withInitial(HashSet::new);

    public UnitOfWorkImpl(
            PlatformTransactionManager transactionManager,
            EventBus eventBus
    ) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.eventBus = eventBus;
    }

    @Override
    public <T> T execute(Supplier<T> action) {
        return transactionTemplate.execute(status -> {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            publishDomainEvents();
                        }
                        @Override
                        public void afterCompletion(int status) {
                            clearTrackedAggregates();
                        }
                    }
            );
            return action.get();
        });
    }

    @Override
    public void registerAggregate(AggregateRoot<?> aggregate) {
        trackedAggregates.get().add(aggregate);
    }

    private void publishDomainEvents() {

        Set<AggregateRoot<?>> aggregates = trackedAggregates.get();

        for (AggregateRoot<?> aggregate : aggregates) {

            List<DomainEvent> events = aggregate.getDomainEvents();

            if (!events.isEmpty()) {
                events.forEach(eventBus::publish);
                aggregate.clearDomainEvents();
            }
        }
    }

    private void clearTrackedAggregates() {
        trackedAggregates.get().clear();
        trackedAggregates.remove();
    }
}