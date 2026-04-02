package buildingblocks.core.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class BaseLogger {

    private final Logger logger;

    public BaseLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    private void injectTraceContext() {

        SpanContext ctx = Span.current().getSpanContext();

        if (ctx.isValid()) {
            MDC.put("trace_id", ctx.getTraceId());
            MDC.put("span_id", ctx.getSpanId());
        } else {
            MDC.put("trace_id", "");
            MDC.put("span_id", "");
        }
    }

    public void info(String message, Object... args) {
        injectTraceContext();
        logger.info(message, args);
    }

    public void warn(String message, Object... args) {
        injectTraceContext();
        logger.warn(message, args);
    }

    public void error(String message, Object... args) {
        injectTraceContext();
        logger.error(message, args);
    }

    public void debug(String message, Object... args) {
        injectTraceContext();
        logger.debug(message, args);
    }
}