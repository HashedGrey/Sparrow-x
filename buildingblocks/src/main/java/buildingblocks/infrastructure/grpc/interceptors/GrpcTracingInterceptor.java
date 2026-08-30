package buildingblocks.infrastructure.grpc.interceptors;

import buildingblocks.infrastructure.observability.GrpcTracingPropagator;
import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;


public class GrpcTracingInterceptor implements ServerInterceptor {

    private static final Logger logger =
            LoggerFactory.getLogger(GrpcTracingInterceptor.class);

    private static final String MDC_OTEL_TRACE_ID = "otel_trace_id";
    private static final String MDC_OTEL_SPAN_ID = "otel_span_id";

    private final Tracer tracer;

    public GrpcTracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String method = call.getMethodDescriptor().getFullMethodName();

        Context parentContext = GrpcTracingPropagator.extractContext(headers);

        Span span = tracer.spanBuilder(method)
                .setSpanKind(SpanKind.SERVER)
                .setParent(parentContext)
                .startSpan();

        SpanContext spanContext = span.getSpanContext();

        MDC.put(MDC_OTEL_TRACE_ID, spanContext.getTraceId());
        MDC.put(MDC_OTEL_SPAN_ID, spanContext.getSpanId());

        logger.info("trace.start: {}", spanContext.getTraceId());

        Context context = parentContext.with(span);

        ServerCall<ReqT, RespT> tracingCall =
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {

                    @Override
                    public void close(Status status, Metadata trailers) {
                        try {
                            if (!status.isOk()) {
                                span.recordException(status.asRuntimeException());
                            }

                            span.setAttribute("rpc.system", "grpc");
                            span.setAttribute("rpc.method", method);
                            span.setAttribute("rpc.grpc.status_code", status.getCode().name());

                            span.end();

                            super.close(status, trailers);
                        } finally {
                            MDC.remove(MDC_OTEL_TRACE_ID);
                            MDC.remove(MDC_OTEL_SPAN_ID);
                        }
                    }
                };

        ServerCall.Listener<ReqT> listener;

        try (Scope scope = context.makeCurrent()) {
            listener = next.startCall(tracingCall, headers);
        }

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {

            @Override
            public void onMessage(ReqT message) {
                try (Scope scope = context.makeCurrent()) {
                    withTraceMdc(span, () -> super.onMessage(message));
                }
            }

            @Override
            public void onHalfClose() {
                try (Scope scope = context.makeCurrent()) {
                    withTraceMdc(span, super::onHalfClose);
                }
            }

            @Override
            public void onCancel() {
                try (Scope scope = context.makeCurrent()) {
                    withTraceMdc(span, super::onCancel);
                }
            }

            @Override
            public void onComplete() {
                try (Scope scope = context.makeCurrent()) {
                    withTraceMdc(span, super::onComplete);
                }
            }

            @Override
            public void onReady() {
                try (Scope scope = context.makeCurrent()) {
                    withTraceMdc(span, super::onReady);
                }
            }
        };
    }

    private void withTraceMdc(Span span, Runnable runnable) {
        SpanContext spanContext = span.getSpanContext();

        MDC.put(MDC_OTEL_TRACE_ID, spanContext.getTraceId());
        MDC.put(MDC_OTEL_SPAN_ID, spanContext.getSpanId());

        try {
            runnable.run();
        } finally {
            MDC.remove(MDC_OTEL_TRACE_ID);
            MDC.remove(MDC_OTEL_SPAN_ID);
        }
    }
}