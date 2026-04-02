package buildingblocks.core.commands;


@FunctionalInterface
public interface CommandExecutionChain<R> {
    R proceed(Command<R> command);
}