package buildingblocks.infrastructure.grpc.interceptors;

import buildingblocks.infrastructure.observability.LoggerFactoryUtil;
import io.grpc.*;
import org.springframework.stereotype.Component;

@Component
public class GrpcLoggingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();

        LoggerFactoryUtil.info(getClass(), "Received gRPC call: {}", methodName);

        ServerCall<ReqT, RespT> loggingCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendMessage(RespT message) {
                LoggerFactoryUtil.info(getClass(), "Sending response for {}: {}", methodName, message);
                super.sendMessage(message);
            }

            @Override
            public void close(Status status, Metadata trailers) {
                LoggerFactoryUtil.info(getClass(), "Closing call {} with status: {}", methodName, status);
                super.close(status, trailers);
            }
        };

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(next.startCall(loggingCall, headers)) {
            @Override
            public void onMessage(ReqT message) {
                LoggerFactoryUtil.info(getClass(), "Received request message for {}: {}", methodName, message);
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                LoggerFactoryUtil.info(getClass(), "Client finished sending messages for {}", methodName);
                super.onHalfClose();
            }

            @Override
            public void onCancel() {
                LoggerFactoryUtil.warn(getClass(), "Call {} was cancelled by the client", methodName);
                super.onCancel();
            }

            @Override
            public void onComplete() {
                LoggerFactoryUtil.info(getClass(), "Call {} completed successfully", methodName);
                super.onComplete();
            }

            @Override
            public void onReady() {
                super.onReady();
            }
        };
    }
}
