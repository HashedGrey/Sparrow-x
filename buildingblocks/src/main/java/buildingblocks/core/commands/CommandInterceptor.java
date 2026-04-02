package buildingblocks.core.commands;

public interface CommandInterceptor {

    <R> R intercept(
            Command<R> command,
            CommandExecutionChain<R> chain
    );
}