package buildingblocks.infrastructure.grpc.interceptors;

import buildingblocks.infrastructure.observability.GrpcTracingPropagator;
import io.grpc.*;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Component;

@Component
public class GrpcTracingInterceptor implements ServerInterceptor {

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("sparrowx-grpc");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();

        // Extract parent context from upstream (Linkerd or other service)
        Context parentContext = GrpcTracingPropagator.extractContext(headers);

        // Start the server span
        Span span = tracer.spanBuilder(methodName)
                .setSpanKind(SpanKind.SERVER)
                .setParent(parentContext)
                .startSpan();

        // Standard span attributes
        span.setAttribute("rpc.system", "grpc");
        span.setAttribute("rpc.method", methodName);

        // Make span current for downstream calls
        Scope scope = parentContext.with(span).makeCurrent();

        // Wrap the ServerCall to end span when call closes
        ServerCall<ReqT, RespT> tracingCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                if (!status.isOk()) {
                    span.recordException(status.asRuntimeException());
                }
                span.setAttribute("rpc.grpc.status_code", status.getCode().name());
                span.end();
                scope.close();
                super.close(status, trailers);
            }
        };

        // Proceed with the call using the wrapped ServerCall
        return next.startCall(tracingCall, headers);
    }
}
