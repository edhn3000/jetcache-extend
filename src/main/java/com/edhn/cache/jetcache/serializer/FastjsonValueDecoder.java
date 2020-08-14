package com.edhn.cache.jetcache.serializer;

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alicp.jetcache.support.AbstractValueDecoder;
import com.alicp.jetcache.support.CacheEncodeException;

/**
 * Created on 2016/10/4.
 *
 * ParserConfig.getGlobalInstance().addAccept("com.company.yourpackage.");
 * DecoderMap.register(FastjsonValueEncoder.IDENTITY_NUMBER, FastjsonValueDecoder.INSTANCE);
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class FastjsonValueDecoder extends AbstractValueDecoder {

    /**
     * @param useIdentityNumber
     */
    public FastjsonValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public Object doApply(byte[] buffer) {
        try {
	        if (useIdentityNumber) {
	            byte[] bs = new byte[buffer.length - FastjsonValueEncoder.IDENTITY_LENGTH];
	            System.arraycopy(buffer, FastjsonValueEncoder.IDENTITY_LENGTH, bs, 0, bs.length);
	            return JSON.parse(bs, Feature.SupportAutoType);
	        } else {
	            return JSON.parse(buffer, Feature.SupportAutoType);
	        }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Fastjson Decode error. ");
            try {
				sb.append("data=").append(new String(buffer, "UTF-8"));
				throw new CacheEncodeException(sb.toString(), e);
			} catch (UnsupportedEncodingException e1) {
				sb.append("try decode to string fail!");
				throw new CacheEncodeException(sb.toString(), e);
			}
        }
    }
}
