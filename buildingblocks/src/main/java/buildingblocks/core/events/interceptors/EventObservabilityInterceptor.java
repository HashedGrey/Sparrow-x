package buildingblocks.core.events.interceptors;

import buildingblocks.core.events.*;
import buildingblocks.core.observability.BaseObservabilityInterceptor;
import buildingblocks.core.observability.BaseTracer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class EventObservabilityInterceptor
        extends BaseObservabilityInterceptor
        implements EventInterceptor {

    private final BaseTracer tracer;

    public EventObservabilityInterceptor(BaseTracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void intercept(DomainEvent event, EventExecutionChain chain) {
        String spanName = "event." + event.getClass().getSimpleName();
        tracer.trace(
                spanName,
                () -> observe(
                        "event",
                        event.getClass().getSimpleName(),
                        () -> {
                            chain.proceed(event);
                            return null;
                        }
                )
        );
    }
}