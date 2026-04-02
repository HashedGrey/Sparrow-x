package buildingblocks.infrastructure.cache;

import com.google.protobuf.Message;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CacheKeyBuilder {

    public static String buildGrpcKey(String method, Object request) {

        return method + ":" + hash(request.toString());
    }

    public static String buildQueryKey(String queryName, Object query) {

        return "query:" + queryName + ":" + hash(query.toString());
    }

    private static String hash(String value) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Cache key hash failed", e);
        }
    }
}