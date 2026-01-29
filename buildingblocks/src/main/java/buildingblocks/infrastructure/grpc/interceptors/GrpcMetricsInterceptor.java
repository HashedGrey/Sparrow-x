package buildingblocks.infrastructure.grpc.interceptors;

import io.grpc.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import buildingblocks.infrastructure.observability.MetricRegistryUtil;

import java.time.Duration;
import java.time.Instant;

@Component
public class GrpcMetricsInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String fullMethod = call.getMethodDescriptor().getFullMethodName();
        Instant start = Instant.now();

        // Increment request counter
        MetricRegistryUtil.counter("grpc.server.requests", "method", fullMethod).increment();

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
                next.startCall(new MetricsServerCall<>(call, fullMethod, start), headers)
        ) {};
    }

    private static class MetricsServerCall<ReqT, RespT>
            extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {

        private final String method;
        private final Instant start;

        protected MetricsServerCall(ServerCall<ReqT, RespT> delegate,
                                    String method,
                                    Instant start) {
            super(delegate);
            this.method = method;
            this.start = start;
        }

        @Override
        public void close(Status status, Metadata trailers) {
            Duration duration = Duration.between(start, Instant.now());

            Timer.builder("grpc.server.latency")
                    .tag("method", method)
                    .tag("status", status.getCode().name())
                    .register(MetricRegistryUtil.registry())
                    .record(duration);

            Counter.builder("grpc.server.responses")
                    .tag("method", method)
                    .tag("status", status.getCode().name())
                    .register(MetricRegistryUtil.registry())
                    .increment();

            super.close(status, trailers);
        }
    }
}
