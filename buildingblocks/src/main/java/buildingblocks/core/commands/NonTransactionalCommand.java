package buildingblocks.core.commands;

/**
 * Marker for commands whose handler must not execute inside the
 * CommandBus UnitOfWork transaction.
 *
 * Intended for long-running orchestration involving external I/O.
 */
public interface NonTransactionalCommand<R> extends Command<R> {
}