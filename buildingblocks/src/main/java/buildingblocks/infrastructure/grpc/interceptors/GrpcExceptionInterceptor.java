package buildingblocks.infrastructure.grpc.interceptors;

import buildingblocks.shared.exceptions.*;
import io.grpc.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GrpcExceptionInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall<ReqT, RespT> wrappedCall =
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {};

        ServerCall.Listener<ReqT> listener = next.startCall(wrappedCall, headers);

        return new ForwardingServerCallListener
                .SimpleForwardingServerCallListener<>(listener) {

            @Override
            public void onMessage(ReqT message) {
                try {
                    super.onMessage(message);
                } catch (Throwable ex) {
                    closeWithException(wrappedCall, ex);
                }
            }

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Throwable ex) {
                    closeWithException(wrappedCall, ex);
                }
            }
        };
    }

    private void closeWithException(ServerCall<?, ?> call, Throwable ex) {

        StatusWithBody statusWithBody = mapExceptionToStatus(ex);

        call.close(statusWithBody.getStatus(), new Metadata());
    }

    private StatusWithBody mapExceptionToStatus(Throwable ex) {

        if (ex instanceof BadRequestException badRequest) {
            return new StatusWithBody(
                    Status.INVALID_ARGUMENT
                            .withDescription(badRequest.getMessage())
                            .withCause(badRequest),
                    badRequest.getErrorMessages()
            );
        }

        if (ex instanceof NotFoundException notFound) {
            return new StatusWithBody(
                    Status.NOT_FOUND
                            .withDescription(notFound.getMessage())
                            .withCause(notFound),
                    List.of(notFound.getMessage())
            );
        }

        if (ex instanceof ConflictException conflict) {
            return new StatusWithBody(
                    Status.ALREADY_EXISTS
                            .withDescription(conflict.getMessage())
                            .withCause(conflict),
                    List.of(conflict.getMessage())
            );
        }

        if (ex instanceof UnauthorizedException unauthorized) {
            return new StatusWithBody(
                    Status.UNAUTHENTICATED
                            .withDescription(unauthorized.getMessage())
                            .withCause(unauthorized),
                    List.of(unauthorized.getMessage())
            );
        }

        if (ex instanceof ForbiddenException forbidden) {
            return new StatusWithBody(
                    Status.PERMISSION_DENIED
                            .withDescription(forbidden.getMessage())
                            .withCause(forbidden),
                    List.of(forbidden.getMessage())
            );
        }

        if (ex instanceof AppException appEx) {
            return new StatusWithBody(
                    Status.FAILED_PRECONDITION
                            .withDescription(appEx.getMessage())
                            .withCause(appEx),
                    List.of(appEx.getMessage())
            );
        }

        if (ex instanceof InternalServerException internal) {
            return new StatusWithBody(
                    Status.INTERNAL
                            .withDescription(internal.getMessage())
                            .withCause(internal),
                    List.of(internal.getMessage())
            );
        }

        // fallback
        return new StatusWithBody(
                Status.UNKNOWN
                        .withDescription(ex.getMessage())
                        .withCause(ex),
                List.of(ex.getMessage())
        );
    }
}