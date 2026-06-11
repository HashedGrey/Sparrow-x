package buildingblocks.core.commands;


public interface CommandBus {
    <R> R dispatch(Command<R> command);

}

