package com.blueant_crm_erp.util.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Cache Utility
 *
 * Centralized utility for cache operations across
 * the BlueAnt CRM ERP Platform.
 *
 * Responsibilities:
 * - Read cache
 * - Write cache
 * - Remove cache
 * - Clear cache
 * - Check cache existence
 *
 * This utility works with Spring Cache abstraction,
 * allowing Redis, Caffeine or any future provider
 * without changing business logic.
 *
 * Used By:
 * - Authentication
 * - User
 * - Role
 * - Lead
 * - Client
 * - Meeting
 * - Service Request
 * - Dashboard
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final CacheManager cacheManager;

    /**
     * Get cache value.
     */
    public <T> Optional<T> get(
            @NonNull String cacheName,
            @NonNull Object key,
            @NonNull Class<T> type) {

        Objects.requireNonNull(cacheName, "Cache name cannot be null.");
        Objects.requireNonNull(key, "Cache key cannot be null.");
        Objects.requireNonNull(type, "Target type cannot be null.");

        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            return Optional.empty();
        }

        Cache.ValueWrapper wrapper = cache.get(key);

        if (wrapper == null || wrapper.get() == null) {
            return Optional.empty();
        }

        Object value = wrapper.get();

        if (!type.isInstance(value)) {
            return Optional.empty();
        }

        return Optional.of(type.cast(value));
    }

    /**
     * Put value into cache.
     */
    public void put(
            @NonNull String cacheName,
            @NonNull Object key,
            @NonNull Object value) {

        Objects.requireNonNull(cacheName, "Cache name cannot be null.");
        Objects.requireNonNull(key, "Cache key cannot be null.");
        Objects.requireNonNull(value, "Cache value cannot be null.");

        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.put(key, value);
        }
    }

    /**
     * Remove a cache entry.
     */
    public void evict(
            @NonNull String cacheName,
            @NonNull Object key) {

        Objects.requireNonNull(cacheName, "Cache name cannot be null.");
        Objects.requireNonNull(key, "Cache key cannot be null.");

        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.evict(key);
        }
    }

    /**
     * Remove all entries from cache.
     */
    public void clear(
            @NonNull String cacheName) {

        Objects.requireNonNull(cacheName, "Cache name cannot be null.");

        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Check whether cache contains key.
     */
    public boolean contains(
            @NonNull String cacheName,
            @NonNull Object key) {

        return get(cacheName, key, Object.class).isPresent();
    }

}