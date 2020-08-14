package com.edhn.cache.jetcache;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.Assert;

import com.alicp.jetcache.support.AbstractValueDecoder;
import com.alicp.jetcache.support.AbstractValueEncoder;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.edhn.cache.jetcache.serializer.FastjsonValueDecoder;
import com.edhn.cache.jetcache.serializer.FastjsonValueEncoder;
import com.edhn.cache.jetcache.serializer.GsonValueDecoder;
import com.edhn.cache.jetcache.serializer.GsonValueEncoder;
import com.edhn.cache.jetcache.serializer.JacksonValueDecoder;
import com.edhn.cache.jetcache.serializer.JacksonValueEncoder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JetCacheValueParserTest {
    
    private static int times = 50000;
    private static TestBean bean;
    
    /**
     * for gson
     */
    public static Gson gson;
    
    public static class GsonDateSerializer implements JsonSerializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {              
            return new JsonPrimitive(src.getTime());    
        }
    }
    
    public static class GsonDateDeserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {        
            return new java.util.Date(json.getAsJsonPrimitive().getAsLong());    
        }
    }
    
    /**
     * for jackson
     */
    static ObjectMapper objectMapper;
    
    private static byte[] beanJsonBytes;
    private static byte[] beanJavaBytes;
    
//    private static Map<String, Class<?>> clazzCache = new ConcurrentHashMap<>();
    
    @BeforeClass
    public static void init() {
        gson = new GsonBuilder()
        .registerTypeAdapter(java.util.Date.class, new GsonDateSerializer()).setDateFormat(DateFormat.LONG)
        .registerTypeAdapter(java.util.Date.class, new GsonDateDeserializer()).setDateFormat(DateFormat.LONG)
        .create();
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        bean = new TestBean();
        bean.setId("appid123");
        bean.setName("app名称");
        bean.setCreateTime(new Date());
        bean.setNum(2);
//        GsonValueEncoder gsonValueEncoder = new GsonValueEncoder(false, gson);
        JavaValueEncoder javaValueEncoder = new JavaValueEncoder(false);
        JacksonValueEncoder jacksonValueEncoder = new JacksonValueEncoder(false, objectMapper);
        beanJsonBytes = jacksonValueEncoder.apply(bean);
        beanJavaBytes = javaValueEncoder.apply(bean);
    }
    
    private <T> void logPerform(Supplier<T> supplier, String testSubject) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < times; i ++) {
            supplier.get();
        }
        log.info("{} loop {} times use {}ms ", testSubject, times, (System.currentTimeMillis() - start));
    }
    
//    private Class<?> getClass(String className, boolean useCache) {
//        if (useCache) {
//            return clazzCache.computeIfAbsent(className, k->{
//                try {
//                    return Class.forName(k);
//                } catch (ClassNotFoundException e) {
//                    return Object.class;
//                }
//            });
//        } else {
//            try {
//                return Class.forName(className);
//            } catch (ClassNotFoundException e) {
//                return null;
//            }
//        }
//    }
    
    private void testJsonCorrectInner(String testName, AbstractValueEncoder encoder, AbstractValueDecoder decoder) {
        byte[] encodeByjackson = encoder.apply(bean);
        Object beanDecode = decoder.apply(encodeByjackson);
        Assert.isTrue(bean.getClass().equals(beanDecode.getClass()), testName + "序列化 + 反序列化后，对象类型错误！");
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field f: fields) {
            try {
                f.setAccessible(true);
                Object value1 = f.get(bean);
                Object value2 = f.get(beanDecode);
                Assert.isTrue(Objects.equals(value1, value2), 
                    testName + "序列化 + 反序列化后，属性（" + f.getName() + "）的值错误");
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error("反射出错！", e);
            }
        }
    }
    
    @Test
    public void test1_jsonCorrect() {
        AbstractValueEncoder encoder = new JacksonValueEncoder(false, objectMapper);
//        log.debug("正确性测试，json内容：{}", new String(encodeByjackson, StandardCharsets.UTF_8));
        AbstractValueDecoder decoder = new JacksonValueDecoder(false, objectMapper);
        testJsonCorrectInner("jackson", encoder, decoder);
        log.info("jackson序列化正确性测试通过！");

        encoder = new GsonValueEncoder(false, gson);
        decoder = new GsonValueDecoder(false, gson);
        testJsonCorrectInner("gson", encoder, decoder);
        log.info("gson序列化正确性测试通过！");

        encoder = new FastjsonValueEncoder(false);
        decoder = new FastjsonValueDecoder(false);
        testJsonCorrectInner("fastjson", encoder, decoder);
        log.info("fastjson序列化正确性测试通过！");
    }
    
    private void testJsonCompatibilityInner(String testName, AbstractValueEncoder encoder, AbstractValueDecoder... decoders) {
        byte[] encodeByjackson = encoder.apply(bean);
        for (AbstractValueDecoder decoder: decoders) {
            Object beanDecode2 = decoder.apply(encodeByjackson);
            Assert.isTrue(bean.getClass().equals(beanDecode2.getClass()), String.format("%s序列化 + %s反序列化后，对象类型错误！", 
                encoder.getClass().getSimpleName(), decoder.getClass().getSimpleName()));

            Field[] fields = bean.getClass().getDeclaredFields();
            for (Field f: fields) {
                try {
                    f.setAccessible(true);
                    Object value1 = f.get(bean);
                    Object value2 = f.get(beanDecode2);
                    Assert.isTrue(Objects.equals(value1, value2), 
                        String.format("%s序列化 + %s反序列化后，属性（" + f.getName() + "）的值错误",
                        encoder.getClass().getSimpleName(), decoder.getClass().getSimpleName()));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    log.error("反射出错！", e);
                }
            }
        }
    }
    
    
    @Test
    public void test2_JsonCompatibility() {
        testJsonCompatibilityInner("jackson(fastjson,gson)", new JacksonValueEncoder(false, objectMapper), 
            new FastjsonValueDecoder(false), new GsonValueDecoder(false, gson));
        testJsonCompatibilityInner("fastjson(jackson,gson)", new FastjsonValueEncoder(false), 
            new JacksonValueDecoder(false, objectMapper), new GsonValueDecoder(false, gson));
        testJsonCompatibilityInner("gson(fastjson,jackson)", new GsonValueEncoder(false, gson), 
            new FastjsonValueDecoder(false), new JacksonValueDecoder(false, objectMapper));
        
        log.info("json序列化兼容性测试通过！");
    }
    
    
    @Test
    public void test9_Perform() {
        /////////// encode 
        JacksonValueEncoder jacksonValueEncoder = new JacksonValueEncoder(false, objectMapper);
        logPerform(()->jacksonValueEncoder.apply(bean), "jackson encode");
        
        GsonValueEncoder gsonValueEncoder = new GsonValueEncoder(false, gson);
        logPerform(()->gsonValueEncoder.apply(bean), "gson encode");
        
        FastjsonValueEncoder fastjsonValueEncoder = new FastjsonValueEncoder(false);
        fastjsonValueEncoder.apply(bean); // fastjson 预热
        logPerform(()->fastjsonValueEncoder.apply(bean), "fastjson encode");
        
        JavaValueEncoder javaValueEncoder = new JavaValueEncoder(false);
        logPerform(()->javaValueEncoder.apply(bean), "java encode");
        log.info("序列化性能测试完毕！");
        
        /////////// decode
        JacksonValueDecoder jacksonValueDecoder = new JacksonValueDecoder(false, objectMapper);
        logPerform(()->jacksonValueDecoder.apply(beanJsonBytes), "jackson decode");
        
        GsonValueDecoder gsonValueDecoder = new GsonValueDecoder(false, gson);
        logPerform(()->gsonValueDecoder.apply(beanJsonBytes), "gson decode");

        FastjsonValueDecoder fastjsonValueDecoder = new FastjsonValueDecoder(false);
        fastjsonValueDecoder.apply(beanJsonBytes); // fastjson 预热
        logPerform(()->fastjsonValueDecoder.apply(beanJsonBytes), "fastjson decode");
        
        JavaValueDecoder javaValueDecoder = new JavaValueDecoder(false);
        logPerform(()->javaValueDecoder.apply(beanJavaBytes), "java decode");
        log.info("反序列化性能测试完毕！");

        /////////// test reflect
//        logPerform(() -> getClass("com.thunisoft.cache.jetcache.JetCacheValueParserTest.TestBean", false), "get class reflect");
//        logPerform(()->getClass("com.thunisoft.cache.jetcache.JetCacheValueParserTest.TestBean", true), "get class with map");
    }
    
    @Data
    public static class TestBean implements Serializable {
        /**         *         */
        private static final long serialVersionUID = 1L;
        
        private String id;
        private String name;
        private Integer num;
        private Date createTime;
        
    }

}
