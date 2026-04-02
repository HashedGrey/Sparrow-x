package buildingblocks.core.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class BaseTracer {

    private final Tracer tracer;

    public BaseTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public <T> T trace(String spanName, Supplier<T> supplier) {
        return trace(spanName, span -> supplier.get());
    }

    public <T> T trace(String spanName, Function<Span, T> callback) {
        Span span = tracer.spanBuilder(spanName)
                .setParent(Context.current())
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (var scope = span.makeCurrent()) {
            return callback.apply(span);
        } catch (Exception ex) {
            span.recordException(ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}