package buildingblocks.core.commands;

import buildingblocks.infrastructure.persistence.UnitOfWork;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CommandBusImpl implements CommandBus {

    private final ApplicationContext applicationContext;
    private final UnitOfWork unitOfWork;
    private final List<CommandInterceptor> interceptors;

    private final ConcurrentHashMap<Class<?>, CommandHandler<?, ?>> handlerCache =
            new ConcurrentHashMap<>();

    public CommandBusImpl(
            ApplicationContext applicationContext,
            UnitOfWork unitOfWork,
            List<CommandInterceptor> interceptors
    ) {
        this.applicationContext = applicationContext;
        this.unitOfWork = unitOfWork;
        this.interceptors = interceptors == null ? List.of() : List.copyOf(interceptors);
    }

    @Override
    public <R> R dispatch(Command<R> command) {

        if (command == null) {
            throw new IllegalArgumentException("Command must not be null");
        }

        CommandHandler<Command<R>, R> handler = resolveHandler(command);

        CommandExecutionChain<R> chain =
                buildInterceptorChain(handler);

        if (command instanceof NonTransactionalCommand<?>) {
            return chain.proceed(command);
        }

        // Transaction boundary
        return unitOfWork.execute(() -> chain.proceed(command));
    }

    @SuppressWarnings("unchecked")
    private <R> CommandHandler<Command<R>, R> resolveHandler(Command<R> command) {

        Class<?> commandType = command.getClass();

        return (CommandHandler<Command<R>, R>) handlerCache.computeIfAbsent(
                commandType,
                type -> {

                    String[] beanNames =
                            applicationContext.getBeanNamesForType(CommandHandler.class);

                    for (String beanName : beanNames) {

                        Class<?> beanClass = applicationContext.getType(beanName);

                        ResolvableType resolvableType =
                                ResolvableType.forClass(beanClass)
                                        .as(CommandHandler.class);

                        Class<?> handlerCommandType =
                                resolvableType.getGeneric(0).resolve();

                        if (handlerCommandType != null &&
                                handlerCommandType.equals(type)) {

                            return applicationContext.getBean(beanName, CommandHandler.class);
                        }
                    }

                    throw new IllegalStateException(
                            "No CommandHandler registered for command: " + type.getName()
                    );
                }
        );
    }
    private <R> CommandExecutionChain<R> buildInterceptorChain(
            CommandHandler<Command<R>, R> handler
    ) {

        CommandExecutionChain<R> chain =
                command -> handler.handle((Command<R>) command);

        // reverse order so first registered interceptor runs first
        for (int i = interceptors.size() - 1; i >= 0; i--) {

            CommandInterceptor interceptor = interceptors.get(i);
            CommandExecutionChain<R> next = chain;

            chain = command -> interceptor.intercept(command, next);
        }

        return chain;
    }
}