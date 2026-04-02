package buildingblocks.core.events;

import buildingblocks.core.commands.Command;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class InternalCommand<R> implements Command<R> {

    private final UUID commandId;
    private final Instant createdAt;

    protected InternalCommand() {
        this.commandId = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

}