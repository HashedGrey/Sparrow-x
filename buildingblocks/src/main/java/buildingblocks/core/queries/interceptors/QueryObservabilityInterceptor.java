package buildingblocks.core.queries.interceptors;

import buildingblocks.core.observability.BaseMetricsCollector;
import buildingblocks.core.observability.BaseObservabilityInterceptor;
import buildingblocks.core.observability.BaseTracer;
import buildingblocks.core.queries.Query;
import buildingblocks.core.queries.QueryExecutionChain;
import buildingblocks.core.queries.QueryInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class QueryObservabilityInterceptor
        extends BaseObservabilityInterceptor
        implements QueryInterceptor {

    private final BaseTracer tracer;
    private final BaseMetricsCollector metrics;

    public QueryObservabilityInterceptor(
            BaseTracer tracer,
            BaseMetricsCollector metrics
    ) {
        this.tracer = tracer;
        this.metrics = metrics;
    }

    @Override
    public <R> R intercept(Query<R> query, QueryExecutionChain<R> chain) {

        String queryName = query.getClass().getSimpleName();
        String spanName = "query." + queryName;
        long start = System.nanoTime();

        return tracer.trace(
                spanName,
                () -> {
                    try {
                        R result = observe("query",
                                queryName,
                                () -> chain.proceed(query)
                        );

                        metrics.increment(
                                "query.executions",
                                "query", queryName,
                                "outcome", "success"
                        );

                        return result;

                    } catch (Exception ex) {

                        metrics.increment(
                                "query.executions",
                                "query", queryName,
                                "outcome", "failure"
                        );

                        throw ex;

                    } finally {

                        long durationNanos = System.nanoTime() - start;

                        metrics.recordTime(
                                "query.duration",
                                durationNanos,
                                "query", queryName
                        );
                    }
                }
        );
    }
}