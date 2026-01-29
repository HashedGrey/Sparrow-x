package buildingblocks.core.queries.interceptors;

import buildingblocks.core.queries.IQuery;
import buildingblocks.core.queries.QueryHandler;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class QueryTracingInterceptor implements QueryInterceptor {

    private final Tracer tracer;

    public QueryTracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public <Q extends IQuery<R>, R> R intercept(Q query, QueryHandler<Q, R> next) {
        Span span = tracer.spanBuilder(query.getClass().getSimpleName())
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("query.type", query.getClass().getSimpleName());
            R response = next.handle(query);
            return response;
        } catch (Exception ex) {
            span.recordException(ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}
