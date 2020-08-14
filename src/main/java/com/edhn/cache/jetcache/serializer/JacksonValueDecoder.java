package com.edhn.cache.jetcache.serializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alicp.jetcache.support.AbstractValueDecoder;
import com.alicp.jetcache.support.CacheEncodeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GsonValueDecoder
 * 
 * @author fengyq
 * @version 1.0
 * @date 2019-07-22
 * 
 */
public class JacksonValueDecoder extends AbstractValueDecoder {

    private ObjectMapper objectMapper;

    private static final Pattern TYPE_PATTERN = Pattern.compile("\"@type\":\"(.+?)\",?");

    private static Map<String, Class<?>> clazzCache = new ConcurrentHashMap<>();

    /**
     * @param useIdentityNumber useIdentityNumber
     */
    public JacksonValueDecoder(boolean useIdentityNumber, ObjectMapper objectMapper) {
        super(useIdentityNumber);
        this.objectMapper = objectMapper;
    }

    @Override
    public Object doApply(byte[] buffer) {
        if (useIdentityNumber) {
            byte[] bs = new byte[buffer.length - FastjsonValueEncoder.IDENTITY_LENGTH];
            System.arraycopy(buffer, FastjsonValueEncoder.IDENTITY_LENGTH, bs, 0, bs.length);
            String json = new String(bs, StandardCharsets.UTF_8);
            try {
                Class<?> cls = parseClass(json);
                return objectMapper.readValue(json, cls);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("jackson decode error!", e);
            }
        } else {
            String json = new String(buffer, StandardCharsets.UTF_8);
            try {
                Class<?> cls = parseClass(json);
                return objectMapper.readValue(json, cls);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("jackson decode error!", e);
            }
        }
    }

    private Class<?> parseClass(String json) {
        Matcher m = TYPE_PATTERN.matcher(json);
        if (m.find()) {
            String className = m.group(1);
            return clazzCache.computeIfAbsent(className, k -> {
                try {
                    return Class.forName(k);
                } catch (ClassNotFoundException e) {
                    throw new CacheEncodeException("jackson decode can't find class=" + className, e);
                }
            });
        } else {
            throw new RuntimeException("jackson decode unkown class");
        }
    }

}
