package buildingblocks.core.commands;

public interface CommandHandler<TCommand extends Command<R>, R> {
    R handle(TCommand command);
}