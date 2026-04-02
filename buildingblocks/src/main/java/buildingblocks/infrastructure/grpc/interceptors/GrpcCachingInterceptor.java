package buildingblocks.infrastructure.grpc.interceptors;

import buildingblocks.infrastructure.cache.CacheKeyBuilder;
import buildingblocks.infrastructure.cache.CacheProvider;
import com.google.protobuf.Message;
import io.grpc.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
public class GrpcCachingInterceptor implements ServerInterceptor {

    private static final long DEFAULT_TTL_SECONDS = 30;

    private final CacheProvider cacheProvider;

    public GrpcCachingInterceptor(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Only cache unary calls
        if (call.getMethodDescriptor().getType() != MethodDescriptor.MethodType.UNARY) {
            return next.startCall(call, headers);
        }

        final String methodName = call.getMethodDescriptor().getFullMethodName();

        final Holder<String> cacheKeyHolder = new Holder<>();

        ServerCall<ReqT, RespT> cachingCall =
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {

                    @Override
                    public void sendMessage(RespT message) {

                        try {

                            String cacheKey = cacheKeyHolder.value;

                            if (cacheKey != null && message instanceof Message proto) {

                                byte[] bytes = proto.toByteArray();

                                cacheProvider.put(
                                        cacheKey,
                                        bytes,
                                        DEFAULT_TTL_SECONDS
                                );
                            }

                        } catch (Exception ignored) {
                            // Cache must never break request flow
                        }

                        super.sendMessage(message);
                    }
                };

        ServerCall.Listener<ReqT> delegate = next.startCall(cachingCall, headers);

        return new ForwardingServerCallListener
                .SimpleForwardingServerCallListener<>(delegate) {

            private ReqT request;

            @Override
            public void onMessage(ReqT message) {

                this.request = message;
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {

                try {

                    if (request instanceof Message protoRequest) {

                        String cacheKey = CacheKeyBuilder.buildGrpcKey(
                                methodName,
                                protoRequest
                        );

                        cacheKeyHolder.value = cacheKey;

                        byte[] cached = cacheProvider.get(cacheKey);

                        if (cached != null) {

                            @SuppressWarnings("unchecked")
                            RespT response = (RespT) call
                                    .getMethodDescriptor()
                                    .getResponseMarshaller()
                                    .parse(new ByteArrayInputStream(cached));

                            call.sendMessage(response);
                            call.close(Status.OK, new Metadata());

                            return;
                        }
                    }

                } catch (Exception ignored) {
                    // Cache failures should never break the request
                }

                super.onHalfClose();
            }
        };
    }

    /**
     * Simple mutable holder for sharing cache key between
     * listener and call wrapper.
     */
    private static class Holder<T> {
        T value;
    }
}