package com.sparrowx.tweet.grpc.policies;

import buildingblocks.infrastructure.cache.CacheProvider;
import buildingblocks.infrastructure.cache.RedisCacheProvider;

import java.time.Duration;

public class TweetCachePolicy implements CacheProvider {

    private final RedisCacheProvider redisCacheProvider;
    private final Duration ttl;
    private final String keyPrefix;

    public TweetCachePolicy(RedisCacheProvider redisCacheProvider, Duration ttl, String keyPrefix) {
        this.redisCacheProvider = redisCacheProvider;
        this.ttl = ttl;
        this.keyPrefix = keyPrefix;
    }

    private String prefixed(String key) {
        return keyPrefix + key;
    }

    @Override
    public void put(String key, byte[] value, long ignoredTtlSeconds) {
        redisCacheProvider.put(prefixed(key), value, ttl.getSeconds());
    }

    @Override
    public byte[] get(String key) {
        return redisCacheProvider.get(prefixed(key));
    }

    @Override
    public void evict(String key) {
        redisCacheProvider.evict(prefixed(key));
    }

    @Override
    public void clear() {
        redisCacheProvider.clear();
    }
}