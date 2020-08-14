/*
 * @(#)JetCacheUtil.java 2019年6月19日 下午4:19:23
 * t3-rule
 * Copyright 2019 Thuisoft, Inc. All rights reserved.
 * THUNISOFT PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.edhn.cache.jetcache.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.support.CacheManager;

/**
 * JetCacheUtil
 * JetCache缓存通用方法，比如通过缓存名获取缓存对象并操作更新
 * @author fengyq
 * @version 1.0
 * @date 2019-06-19
 */
public final class JetCacheUtil {

    private JetCacheUtil() {

    }

    /**
     * 移除缓存内容
     * @param cacheName
     * @param key
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void remove(String cacheName, String key) {
        if (key == null) {
            return;
        }
        Cache cache = CacheManager.defaultManager().getCache(cacheName);
        Optional.ofNullable(cache).ifPresent(c -> c.remove(key));
    }


    /**
     * 移除缓存内容
     * @param cacheName
     * @param keys
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void removeAll(String cacheName, Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        Cache cache = CacheManager.defaultManager().getCache(cacheName);
        Optional.ofNullable(cache).ifPresent(c -> c.removeAll(keys));
    }

    /**
     * 获取缓存内容
     * @param cacheName
     * @param key
     * @param c
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T get(String cacheName, String key, Class<T> c) {
        Cache<String, T> cache = CacheManager.defaultManager().getCache(cacheName);
        return cache == null ? null : cache.get(key);
    }

    /**
     * 获取缓存内容
     * @param cacheName
     * @param keys
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Map<String, T> getAll(String cacheName, Set<String> keys, Class<T> c) {
        Cache<String, T> cache = CacheManager.defaultManager().getCache(cacheName);
        return (cache == null || keys == null) ? new HashMap<String, T>() : cache.getAll(keys);
    }


    /**
     * 设置缓存内容
     * @param cacheName
     * @param key
     * @param value
     */
    @SuppressWarnings({"unchecked"})
    public static void put(String cacheName, String key, Object value) {
        Cache<String, Object> cache = CacheManager.defaultManager().getCache(cacheName);
        Optional.ofNullable(cache).ifPresent(c -> c.put(key, value));
    }

}
