package buildingblocks.infrastructure.cache;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisCacheProvider implements CacheProvider {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisCacheProvider(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void put(String key, String value, Duration ttl, String keyPrefix) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(keyPrefix + key, value, ttl);
    }

    public String get(String key, String keyPrefix) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        return ops.get(keyPrefix + key);
    }

    public void evict(String key, String keyPrefix) {
        redisTemplate.delete(keyPrefix + key);
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("Use put with TTL and key prefix for service-specific caching");
    }

    @Override
    public String get(String key) {
        throw new UnsupportedOperationException("Use get with key prefix for service-specific caching");
    }

    @Override
    public void evict(String key) {
        throw new UnsupportedOperationException("Use evict with key prefix for service-specific caching");
    }

    @Override
    public void clear() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushDb();
            return null;
        });
    }
}
