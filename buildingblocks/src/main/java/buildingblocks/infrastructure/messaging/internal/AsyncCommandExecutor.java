package buildingblocks.infrastructure.messaging.internal;

import buildingblocks.shared.context.CorrelationContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.function.Supplier;

@Component
public class AsyncCommandExecutor {
    private final ExecutorService executor;
    public AsyncCommandExecutor() {
        this.executor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("async-command-", 0)
                        .factory()
        );
    }

    public <T> CompletableFuture<T> submit(Supplier<T> task) {
        String correlationId = CorrelationContext.getCorrelationId();
        Context context = Context.current();
        return CompletableFuture.supplyAsync(() -> {
            if (correlationId != null) {
                CorrelationContext.setCorrelationId(correlationId);
            }
            try (Scope ignored = context.makeCurrent()) {
                return task.get();
            }
            finally {
                CorrelationContext.clear();
            }

        }, executor);
    }
}