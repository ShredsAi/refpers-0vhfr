package ai.shreds.application.ports;

public interface ApplicationCacheOutputPort {

    /**
     * Store a value in cache with a time-to-live.
     * @param key cache key
     * @param value object to cache
     * @param ttl time to live in milliseconds
     */
    void put(String key, Object value, long ttl);

    /**
     * Retrieve a value from cache.
     * @param key cache key
     * @return cached object or null if not found
     */
    Object get(String key);

    /**
     * Delete a key from cache.
     * @param key cache key
     */
    void delete(String key);

    /**
     * Check if a key exists in cache.
     * @param key cache key
     * @return true if exists, false otherwise
     */
    boolean exists(String key);
}
