package com.edhn.cache.jetcache.serializer;

import java.nio.charset.StandardCharsets;

import com.alicp.jetcache.support.AbstractValueEncoder;
import com.alicp.jetcache.support.CacheEncodeException;
import com.google.gson.Gson;

/**
 * GsonValueEncoder
 * 
 * @author fengyq
 * @version 1.0
 * @date 2019-07-22
 * 
 */
public class GsonValueEncoder extends AbstractValueEncoder {
    
    private final Gson gson;

    protected static final int IDENTITY_NUMBER = 0x4A953A81;
    protected static final int IDENTITY_LENGTH = 4;

    /**
     * @param useIdentityNumber useIdentityNumber
     * @param gson gson
     */
    public GsonValueEncoder(boolean useIdentityNumber, Gson gson) {
        super(useIdentityNumber);
        this.gson = gson;
    }

    @Override
    public byte[] apply(Object value) {
        try {
            String json = gson.toJson(value);
            // 以@type为key写入classname，兼容fastjson在json中包含类名的方式
            json = "{\"@type\":\"" + value.getClass().getName() + "\"," + (json.startsWith("{") ? json.substring(1) : json);
            byte[] bs1 = json.getBytes(StandardCharsets.UTF_8);

            if (useIdentityNumber) {
                byte[] bs2 = new byte[bs1.length + IDENTITY_LENGTH];
                writeHeader(bs2, IDENTITY_NUMBER);
                System.arraycopy(bs1, 0, bs2, IDENTITY_LENGTH, bs1.length);
                return bs2;
            } else {
                return bs1;
            }
        } catch (Exception e) {
            throw new CacheEncodeException("gson encode error!", e);
        }
    }

}
