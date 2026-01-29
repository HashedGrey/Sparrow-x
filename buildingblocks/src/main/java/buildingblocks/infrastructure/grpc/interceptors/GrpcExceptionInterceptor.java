package buildingblocks.infrastructure.grpc.interceptors;

import buildingblocks.shared.exceptions.*;
import io.grpc.*;

import java.util.List;

public class GrpcExceptionInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                super.close(status, trailers);
            }
        };

        try {
            return next.startCall(wrappedCall, headers);
        } catch (Exception ex) {
            StatusWithBody statusWithBody = mapExceptionToStatus(ex);
            wrappedCall.close(statusWithBody.getStatus(), new Metadata());
            return new ServerCall.Listener<>() {
            }; // noop listener
        }

    }

    private StatusWithBody mapExceptionToStatus(Exception ex) {
        if (ex instanceof BadRequestException badRequest) {
            return new StatusWithBody(
                    Status.INVALID_ARGUMENT.withDescription(badRequest.getMessage()).withCause(badRequest),
                    badRequest.getErrorMessages()
            );
        } else if (ex instanceof NotFoundException notFound) {
            return new StatusWithBody(
                    Status.NOT_FOUND.withDescription(notFound.getMessage()).withCause(notFound),
                    List.of(notFound.getMessage())
            );
        } else if (ex instanceof ConflictException conflict) {
            return new StatusWithBody(
                    Status.ALREADY_EXISTS.withDescription(conflict.getMessage()).withCause(conflict),
                    List.of(conflict.getMessage())
            );
        } else if (ex instanceof UnauthorizedException unauthorized) {
            return new StatusWithBody(
                    Status.UNAUTHENTICATED.withDescription(unauthorized.getMessage()).withCause(unauthorized),
                    List.of(unauthorized.getMessage())
            );
        } else if (ex instanceof ForbiddenException forbidden) {
            return new StatusWithBody(
                    Status.PERMISSION_DENIED.withDescription(forbidden.getMessage()).withCause(forbidden),
                    List.of(forbidden.getMessage())
            );
        } else {
            if (ex instanceof AppException appEx) {
                return new StatusWithBody(
                        Status.FAILED_PRECONDITION.withDescription(appEx.getMessage()).withCause(appEx),
                        List.of(appEx.getMessage())
                );
            } else if (ex instanceof InternalServerException internal) {
                return new StatusWithBody(
                        Status.INTERNAL.withDescription(internal.getMessage()).withCause(internal),
                        List.of(internal.getMessage())
                );
            }
        }

        // fallback
        return new StatusWithBody(
                Status.UNKNOWN.withDescription(ex.getMessage()).withCause(ex),
                List.of(ex.getMessage())
        );
    }

}
