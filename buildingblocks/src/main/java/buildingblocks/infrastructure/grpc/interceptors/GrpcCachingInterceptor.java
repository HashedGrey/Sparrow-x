package buildingblocks.infrastructure.grpc.interceptors;

import io.grpc.*;
import buildingblocks.infrastructure.cache.CacheProvider;
import buildingblocks.shared.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class GrpcCachingInterceptor implements ServerInterceptor {


    private final CacheProvider cacheProvider;

    public GrpcCachingInterceptor(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Wrap the ServerCall to intercept responses
        ServerCall<ReqT, RespT> cachingCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendMessage(RespT message) {
                // Cache the response
                String cacheKey = generateCacheKey(call.getMethodDescriptor().getFullMethodName(), message);
                cacheProvider.put(cacheKey, JsonUtils.serializeObject(message));
                super.sendMessage(message);
            }
        };

        // Wrap the listener to intercept requests
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(next.startCall(cachingCall, headers)) {

            @Override
            public void onMessage(ReqT request) {
                try {
                    String cacheKey = generateCacheKey(call.getMethodDescriptor().getFullMethodName(), request);
                    String cachedResponseJson = cacheProvider.get(cacheKey);
                    if (cachedResponseJson != null) {
                        // Deserialize and return cached response immediately
                        RespT cachedResponse = (RespT) JsonUtils.deserializeObject(cachedResponseJson, Object.class);
                        cachingCall.sendMessage(cachedResponse);
                        cachingCall.close(Status.OK, new Metadata());
                        return;
                    }
                } catch (Exception e) {
                    // If cache fails, fallback to normal execution
                }
                super.onMessage(request);
            }
        };
    }

    private <T> String generateCacheKey(String methodName, T requestOrResponse) {
        try {
            String json = JsonUtils.serializeObject(requestOrResponse);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(methodName).append(":");
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to generate cache key", e);
        }
    }
}
