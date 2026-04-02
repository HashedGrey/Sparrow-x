package buildingblocks.core.commands.interceptors;

import buildingblocks.core.commands.Command;
import buildingblocks.core.commands.CommandExecutionChain;
import buildingblocks.core.commands.CommandInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class CommandAuthInterceptor implements CommandInterceptor {

    @Override
    public <R> R intercept(
            Command<R> command,
            CommandExecutionChain<R> chain
    ) {

        checkPermissions(command);

        return chain.proceed(command);
    }

    private void checkPermissions(Command<?> command) {
        // use AuthContext / roles / scopes
    }
}