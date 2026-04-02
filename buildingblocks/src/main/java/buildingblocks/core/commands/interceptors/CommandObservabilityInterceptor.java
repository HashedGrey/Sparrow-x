package buildingblocks.core.commands.interceptors;

import buildingblocks.core.commands.Command;
import buildingblocks.core.commands.CommandExecutionChain;
import buildingblocks.core.commands.CommandInterceptor;
import buildingblocks.core.observability.BaseObservabilityInterceptor;
import buildingblocks.core.observability.BaseTracer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class CommandObservabilityInterceptor
        extends BaseObservabilityInterceptor
        implements CommandInterceptor {

    private final BaseTracer tracer;
    public CommandObservabilityInterceptor(BaseTracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public <R> R intercept(Command<R> command, CommandExecutionChain<R> chain) {

        String spanName = "command." + command.getClass().getSimpleName();

        return tracer.trace(
                spanName,
                () -> observe(
                        "command",
                        command.getClass().getSimpleName(),
                        () -> chain.proceed(command)
                )
        );
    }

}