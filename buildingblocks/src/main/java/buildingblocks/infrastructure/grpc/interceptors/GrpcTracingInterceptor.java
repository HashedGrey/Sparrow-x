package buildingblocks.infrastructure.grpc.interceptors;

import buildingblocks.infrastructure.observability.GrpcTracingPropagator;
import io.grpc.*;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GrpcTracingInterceptor implements ServerInterceptor {

    private final Tracer tracer;

    public GrpcTracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }
    private static final Logger logger =
            LoggerFactory.getLogger(GrpcTracingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String method = call.getMethodDescriptor().getFullMethodName();

        // Extract upstream trace context (gateway or other service)
        Context parentContext = GrpcTracingPropagator.extractContext(headers);

        Span span = tracer.spanBuilder(method)
                .setSpanKind(SpanKind.SERVER)
                .setParent(parentContext)
                .startSpan();
        logger.info("trace.start: {}", span.getSpanContext().getTraceId());

        Context context = parentContext.with(span);

        ServerCall<ReqT, RespT> tracingCall =
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {

                    @Override
                    public void close(Status status, Metadata trailers) {

                        if (!status.isOk()) {
                            span.recordException(status.asRuntimeException());
                        }

                        span.setAttribute("rpc.system", "grpc");
                        span.setAttribute("rpc.method", method);
                        span.setAttribute("rpc.grpc.status_code", status.getCode().name());

                        span.end();

                        super.close(status, trailers);
                    }
                };

        ServerCall.Listener<ReqT> listener;

        // activate span for service logic
        try (Scope scope = context.makeCurrent()) {
            listener = next.startCall(tracingCall, headers);
        }

        return new ForwardingServerCallListener
                .SimpleForwardingServerCallListener<>(listener) {

            @Override
            public void onMessage(ReqT message) {
                try (Scope scope = context.makeCurrent()) {
                    super.onMessage(message);
                }
            }

            @Override
            public void onHalfClose() {
                try (Scope scope = context.makeCurrent()) {
                    super.onHalfClose();
                }
            }

            @Override
            public void onCancel() {
                try (Scope scope = context.makeCurrent()) {
                    super.onCancel();
                }
            }

            @Override
            public void onComplete() {
                try (Scope scope = context.makeCurrent()) {
                    super.onComplete();
                }
            }

            @Override
            public void onReady() {
                try (Scope scope = context.makeCurrent()) {
                    super.onReady();
                }
            }
        };
    }
}