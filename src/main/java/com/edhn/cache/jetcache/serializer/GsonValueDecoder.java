package com.edhn.cache.jetcache.serializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alicp.jetcache.support.AbstractValueDecoder;
import com.alicp.jetcache.support.CacheEncodeException;
import com.google.gson.Gson;

/**
 * GsonValueDecoder
 * 
 * @author fengyq
 * @version 1.0
 * @date 2019-07-22
 * 
 */
public class GsonValueDecoder extends AbstractValueDecoder {
    
    private final Gson gson;
    
    private static final Pattern TYPE_PATTERN = Pattern.compile("\"@type\":\"(.+?)\"");
    
    private static Map<String, Class<?>> clazzCache = new ConcurrentHashMap<>();

    /**
     * @param useIdentityNumber useIdentityNumber
     * @param gson gson
     */
    public GsonValueDecoder(boolean useIdentityNumber, Gson gson) {
        super(useIdentityNumber);
        this.gson = gson;
    }

    @Override
    public Object doApply(byte[] buffer) {
        if (useIdentityNumber) {
            byte[] bs = new byte[buffer.length - GsonValueEncoder.IDENTITY_LENGTH];
            System.arraycopy(buffer, GsonValueEncoder.IDENTITY_LENGTH, bs, 0, bs.length);
            String json = new String(bs, StandardCharsets.UTF_8);
            Class<?> cls = parseClass(json);
            return gson.fromJson(json, cls);
        } else {
            String json = new String(buffer, StandardCharsets.UTF_8);
            Class<?> cls = parseClass(json);
            return gson.fromJson(json, cls);
        }
    }
    
    private Class<?> parseClass(String json) {
        Matcher m = TYPE_PATTERN.matcher(json);
        if (m.find()) {
            String className = m.group(1);
            return clazzCache.computeIfAbsent(className, k-> {
                try {
                    return Class.forName(k);
                } catch (ClassNotFoundException e) {
                    throw new CacheEncodeException("gson decode can't find class=" + className, e);
                }
            });
        } else {
            throw new RuntimeException("gson decode unkown class");
        }
    }
    
}
