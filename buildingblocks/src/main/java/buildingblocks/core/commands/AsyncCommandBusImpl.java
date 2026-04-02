package buildingblocks.core.commands;

import buildingblocks.infrastructure.messaging.internal.AsyncCommandExecutor;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.MDC;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class AsyncCommandBusImpl implements AsyncCommandBus {

    private final AsyncCommandExecutor executor;
    private final CommandBus commandBus;

    public AsyncCommandBusImpl(
            AsyncCommandExecutor executor,
            @Lazy CommandBus commandBus
    ) {
        this.executor = executor;
        this.commandBus = commandBus;
    }

    @Override
    public <R> CompletableFuture<R> dispatchAsync(Command<R> command) {

        if (command == null) {
            throw new IllegalArgumentException("Command must not be null");
        }

        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        Context context = Context.current();   // capture OTel context
        return executor.submit(() -> {
            try (Scope scope = context.makeCurrent()) {   // restore context
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                try {
                    return commandBus.dispatch(command);
                } finally {
                    MDC.clear();
                }
            }
        });
    }
}