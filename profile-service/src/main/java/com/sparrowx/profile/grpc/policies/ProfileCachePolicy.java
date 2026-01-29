package com.sparrowx.profile.grpc.policies;

import buildingblocks.infrastructure.cache.CacheProvider;
import buildingblocks.infrastructure.cache.RedisCacheProvider;

import java.time.Duration;

public class ProfileCachePolicy implements CacheProvider {

    private final RedisCacheProvider redisCacheProvider;
    private final Duration ttl;
    private final String keyPrefix;

    public ProfileCachePolicy(RedisCacheProvider redisCacheProvider, Duration ttl, String keyPrefix) {
        this.redisCacheProvider = redisCacheProvider;
        this.ttl = ttl;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void put(String key, String value) {
        redisCacheProvider.put(key, value, ttl, keyPrefix);
    }

    @Override
    public String get(String key) {
        return redisCacheProvider.get(key, keyPrefix);
    }

    @Override
    public void evict(String key) {
        redisCacheProvider.evict(key, keyPrefix);
    }

    @Override
    public void clear() {
        redisCacheProvider.clear();
    }
}
