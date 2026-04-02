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

        LoggerFactoryUtil.info(getClass(), "grpc.call.start {}", methodName);

        ServerCall<ReqT, RespT> loggingCall =
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {

                    @Override
                    public void sendMessage(RespT message) {
                        LoggerFactoryUtil.info(getClass(), "grpc.response.send {}", methodName);
                        super.sendMessage(message);
                    }

                    @Override
                    public void close(Status status, Metadata trailers) {
                        LoggerFactoryUtil.info(
                                getClass(),
                                "grpc.call.close {} status={}",
                                methodName,
                                status
                        );
                        super.close(status, trailers);
                    }
                };

        return new ForwardingServerCallListener
                .SimpleForwardingServerCallListener<>(next.startCall(loggingCall, headers)) {

            @Override
            public void onMessage(ReqT message) {
                LoggerFactoryUtil.info(getClass(), "grpc.request.message {}", methodName);
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                LoggerFactoryUtil.info(getClass(), "grpc.request.complete {}", methodName);
                super.onHalfClose();
            }

            @Override
            public void onCancel() {
                LoggerFactoryUtil.warn(getClass(), "grpc.call.cancel {}", methodName);
                super.onCancel();
            }

            @Override
            public void onComplete() {
                LoggerFactoryUtil.info(getClass(), "grpc.call.complete {}", methodName);
                super.onComplete();
            }

            @Override
            public void onReady() {
                super.onReady();
            }
        };
    }
}

