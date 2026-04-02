package buildingblocks.core.commands;

import java.util.concurrent.CompletableFuture;

public interface AsyncCommandBus {
    <R> CompletableFuture<R> dispatchAsync(Command<R> command);
}