package buildingblocks.infrastructure.cache;

public interface CacheProvider {

    /**
     * Put a value in the cache
     *
     * @param key   cache key
     * @param value cache value as string (serialized)
     */
    void put(String key, String value);

    /**
     * Get a value from the cache
     *
     * @param key cache key
     * @return cached value, or null if missing
     */
    String get(String key);

    /**
     * Remove a value from the cache
     *
     * @param key cache key
     */
    void evict(String key);

    /**
     * Clear all cache (optional, depending on provider)
     */
    void clear();
}
