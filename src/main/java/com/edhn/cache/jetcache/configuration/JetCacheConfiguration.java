package com.edhn.cache.jetcache.configuration;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alicp.jetcache.anno.support.DefaultEncoderParser;
import com.alicp.jetcache.anno.support.EncoderParser;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.edhn.cache.jetcache.lock.JetCacheLockService;
import com.edhn.cache.jetcache.serializer.FastjsonValueDecoder;
import com.edhn.cache.jetcache.serializer.FastjsonValueEncoder;
import com.edhn.cache.jetcache.serializer.JacksonValueDecoder;
import com.edhn.cache.jetcache.serializer.JacksonValueEncoder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JetCacheConfiguration
 * 
 * @author fengyq
 * @version 1.0
 * @date 2019-07-29
 * 
 */
@Configuration
public class JetCacheConfiguration {
    
    private static ObjectMapper objectMapper;
    
    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
     * jetcache序列化器
     * @return json parser
     */
    @Bean(name = "jsonSupportedEncoderParser")
    public EncoderParser jsonSupportedEncoderParser() {
        return new DefaultEncoderParser() {
            @Override
            public Function<Object, byte[]> parseEncoder(String valueEncoder) {
                URI uri = URI.create(valueEncoder.trim());
                String encoder = uri.getPath();
                boolean useIdentityNumber = Optional.ofNullable(uri.getQuery())
                        .map(q -> parseQueryParameters(q))
                        .map(m -> "true".equalsIgnoreCase(m.get("useIdentityNumber"))).orElse(false);
                if ("fastjson".equals(encoder)) {
                    return new FastjsonValueEncoder(useIdentityNumber);
                } else if ("jackson".equals(encoder)) {
                    return new JacksonValueEncoder(useIdentityNumber, objectMapper);
                } else {
                    return super.parseEncoder(valueEncoder);
                }
            }

            @Override
            public Function<byte[], Object> parseDecoder(String valueDecoder) {
                URI uri = URI.create(valueDecoder.trim());
                String decoder = uri.getPath();
                boolean useIdentityNumber = Optional.ofNullable(uri.getQuery())
                        .map(q -> parseQueryParameters(q))
                        .map(m -> "true".equalsIgnoreCase(m.get("useIdentityNumber"))).orElse(false);
                if ("fastjson".equals(decoder)) {
                    return new FastjsonValueDecoder(useIdentityNumber);
                } else if ("jackson".equals(decoder)) {
                    return new JacksonValueDecoder(useIdentityNumber, objectMapper);
                } else {
                    return super.parseDecoder(valueDecoder);
                }
            }};
    }
    
    /**
     * 2.6应该只注册EncoderParser的bean就行，但使用lettuce时存在bug，先使用旧方式覆盖SpringConfigProvider
     * @param parser custom parser
     * @return
     */
    @Bean
    public SpringConfigProvider springConfigProvider(
            @Qualifier(value = "jsonSupportedEncoderParser") EncoderParser parser) {
        return new SpringConfigProvider() {
            @Override
            public Function<Object, byte[]> parseValueEncoder(String valueEncoder) {
                return parser.parseEncoder(valueEncoder);
            }

            @Override
            public Function<byte[], Object> parseValueDecoder(String valueDecoder) {
                return parser.parseDecoder(valueDecoder);
            }
        };
    }
    
    /**
     * @return
     */
    @Bean
    public JetCacheLockService jetCacheLockService() {
        return new JetCacheLockService();
    }

}
