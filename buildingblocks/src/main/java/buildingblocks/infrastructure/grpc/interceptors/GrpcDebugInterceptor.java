package buildingblocks.infrastructure.grpc.interceptors;

import buildingblocks.infrastructure.config.DebugConfig;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A gRPC interceptor that logs debug info conditionally based on a feature toggle.
 */
@Component
public class GrpcDebugInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcDebugInterceptor.class);
    private final DebugConfig debugConfig;

    public GrpcDebugInterceptor(DebugConfig debugConfig) {
        this.debugConfig = debugConfig;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        if (!debugConfig.isGrpcDebugEnabled()) {
            return next.startCall(call, headers);
        }

        log.debug("[gRPC DEBUG] Starting call: {} with headers: {}", call.getMethodDescriptor().getFullMethodName(), headers);

        ServerCall.Listener<ReqT> delegate = next.startCall(
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
                    @Override
                    public void sendMessage(RespT message) {
                        log.debug("[gRPC DEBUG] Sending response: {}", message);
                        super.sendMessage(message);
                    }

                    @Override
                    public void close(Status status, Metadata trailers) {
                        log.debug("[gRPC DEBUG] Call closed with status: {}", status);
                        super.close(status, trailers);
                    }
                },
                headers
        );

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onMessage(ReqT message) {
                log.debug("[gRPC DEBUG] Received message: {}", message);
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                log.debug("[gRPC DEBUG] Client finished sending messages");
                super.onHalfClose();
            }

            @Override
            public void onCancel() {
                log.debug("[gRPC DEBUG] Call cancelled by client");
                super.onCancel();
            }

            @Override
            public void onComplete() {
                log.debug("[gRPC DEBUG] Call completed successfully");
                super.onComplete();
            }

            @Override
            public void onReady() {
                log.debug("[gRPC DEBUG] Call ready to send more messages");
                super.onReady();
            }
        };
    }
}
