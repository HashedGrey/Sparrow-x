package buildingblocks.core.events;

import buildingblocks.core.commands.AsyncCommandBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventBusImpl implements EventBus {

    private final ApplicationContext applicationContext;
    private final List<EventInterceptor> interceptors;
    private final EventMapper eventMapper;
    private final AsyncCommandBus asyncCommandBus;
    private static final Logger log =
            LoggerFactory.getLogger(EventBusImpl.class);

    private final ConcurrentHashMap<Class<?>, List<EventHandler<?>>> handlerCache =
            new ConcurrentHashMap<>();

    public EventBusImpl(
            ApplicationContext applicationContext,
            List<EventInterceptor> interceptors,
            EventMapper eventMapper,
            AsyncCommandBus asyncCommandBus
    ) {
        this.applicationContext = applicationContext;
        this.interceptors = interceptors == null ? List.of() : interceptors;
        this.eventMapper = eventMapper;
        this.asyncCommandBus = asyncCommandBus;}

    @Override
    public void publish(DomainEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }

        log.info("event.publish {}", event.getClass().getSimpleName());


        List<EventHandler<DomainEvent>> handlers =
                resolveHandlers(event);

        for (EventHandler<DomainEvent> handler : handlers) {
            log.debug("event.dispatch {} handler={}",
                    event.getClass().getSimpleName(),
                    handler.getClass().getSimpleName());

            EventExecutionChain chain =
                    buildInterceptorChain(event, handler);

            chain.proceed(event);
        }
        // 2. Map event → internal command
        eventMapper.mapToInternalCommand(event)
                .ifPresent(cmd -> {
                    log.info("event.map {} command={}",
                            event.getClass().getSimpleName(),
                            cmd.getClass().getSimpleName());
                    asyncCommandBus.dispatchAsync(cmd);
                });
    }

    @SuppressWarnings("unchecked")
    private List<EventHandler<DomainEvent>> resolveHandlers(DomainEvent event) {

        return (List<EventHandler<DomainEvent>>) (List<?>)
                handlerCache.computeIfAbsent(
                        event.getClass(),
                        eventType -> {

                            ResolvableType handlerType =
                                    ResolvableType.forClassWithGenerics(
                                            EventHandler.class,
                                            eventType
                                    );

                            String[] beanNames =
                                    applicationContext.getBeanNamesForType(handlerType);

                            return java.util.Arrays.stream(beanNames)
                                    .map(name -> (EventHandler<?>) applicationContext.getBean(name))
                                    .collect(java.util.stream.Collectors.toList());
                        }
                );
    }

    private EventExecutionChain buildInterceptorChain(
            DomainEvent event,
            EventHandler<DomainEvent> handler
    ) {

        EventExecutionChain finalHandler = e -> handler.handle((DomainEvent) e);

        EventExecutionChain chain = finalHandler;

        for (int i = interceptors.size() - 1; i >= 0; i--) {

            EventInterceptor interceptor = interceptors.get(i);
            EventExecutionChain next = chain;

            chain = e -> interceptor.intercept(e, next);
        }

        return chain;
    }
}