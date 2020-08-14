package com.edhn.cache.jetcache.serializer;

import java.nio.charset.StandardCharsets;

import com.alicp.jetcache.support.AbstractValueEncoder;
import com.alicp.jetcache.support.CacheEncodeException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GsonValueEncoder
 * 
 * @author fengyq
 * @version 1.0
 * @date 2019-07-22
 * 
 */
public class JacksonValueEncoder extends AbstractValueEncoder {

    private ObjectMapper objectMapper;

    /**
     * @param useIdentityNumber useIdentityNumber
     * @param gson gson
     */
    public JacksonValueEncoder(boolean useIdentityNumber, ObjectMapper objectMapper) {
        super(useIdentityNumber);
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] apply(Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            // 以@type为key写入classname，兼容fastjson在json中包含类名的方式
            json = "{\"@type\":\"" + value.getClass().getName() + "\"," + (json.startsWith("{") ? json.substring(1) : json);
            byte[] bs1 = json.getBytes(StandardCharsets.UTF_8);

            if (useIdentityNumber) {
                byte[] bs2 = new byte[bs1.length + FastjsonValueEncoder.IDENTITY_LENGTH];
                writeHeader(bs2, FastjsonValueEncoder.IDENTITY_NUMBER);
                System.arraycopy(bs1, 0, bs2, FastjsonValueEncoder.IDENTITY_LENGTH, bs1.length);
                return bs2;
            } else {
                return bs1;
            }
        } catch (Exception e) {
            throw new CacheEncodeException("gson encode error!", e);
        }
    }

}
