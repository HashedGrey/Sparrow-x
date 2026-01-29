package buildingblocks.core.queries.interceptors;

import buildingblocks.core.queries.IQuery;
import buildingblocks.core.queries.QueryHandler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class QueryMetricsInterceptor implements QueryInterceptor {

    private final MeterRegistry meterRegistry;

    public QueryMetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public <Q extends IQuery<R>, R> R intercept(Q query, QueryHandler<Q, R> next) {
        R response = next.handle(query);
        meterRegistry.counter("queries.executed", "type", query.getClass().getSimpleName()).increment();
        return response;
    }
}
