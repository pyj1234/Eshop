package com.cat.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略空值
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
    
    /**
     * 获取ObjectMapper实例
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * 将对象转换为JSON字符串
     * @param object 要转换的对象
     * @return JSON字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("对象转JSON失败: {}", object.getClass().getName(), e);
            throw new RuntimeException("对象转JSON失败", e);
        }
    }
    
    /**
     * 将对象转换为格式化的JSON字符串
     * @param object 要转换的对象
     * @return 格式化的JSON字符串
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("对象转格式化JSON失败: {}", object.getClass().getName(), e);
            throw new RuntimeException("对象转格式化JSON失败", e);
        }
    }
    
    /**
     * 将JSON字符串转换为指定类型的对象
     * @param json JSON字符串
     * @param clazz 目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("JSON转对象失败: {}", json, e);
            throw new RuntimeException("JSON转对象失败", e);
        }
    }
    
    /**
     * 将JSON字符串转换为指定类型的对象（支持泛型）
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            logger.error("JSON转对象失败: {}", json, e);
            throw new RuntimeException("JSON转对象失败", e);
        }
    }
    
    /**
     * 将JSON字符串转换为Map
     * @param json JSON字符串
     * @return Map对象
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * 将JSON字符串转换为List
     * @param json JSON字符串
     * @param elementType 列表元素类型
     * @return List对象
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementType) {
        try {
            CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            return objectMapper.readValue(json, collectionType);
        } catch (JsonProcessingException e) {
            logger.error("JSON转List失败: {}", json, e);
            throw new RuntimeException("JSON转List失败", e);
        }
    }
    
    /**
     * 将对象转换为Map
     * @param object 要转换的对象
     * @return Map对象
     */
    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) {
            return null;
        }
        
        return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * 将Map转换为指定类型的对象
     * @param map Map对象
     * @param clazz 目标类型
     * @return 转换后的对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        
        return objectMapper.convertValue(map, clazz);
    }
    
    /**
     * 检查字符串是否为有效的JSON
     * @param json 要检查的字符串
     * @return 是否为有效的JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * 安全地将对象转换为JSON字符串，转换失败时返回默认值
     * @param object 要转换的对象
     * @param defaultValue 默认值
     * @return JSON字符串或默认值
     */
    public static String toJsonSafely(Object object, String defaultValue) {
        try {
            return toJson(object);
        } catch (Exception e) {
            logger.warn("对象转JSON失败，返回默认值", e);
            return defaultValue;
        }
    }
    
    /**
     * 安全地将JSON字符串转换为对象，转换失败时返回默认值
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param defaultValue 默认值
     * @return 转换后的对象或默认值
     */
    public static <T> T fromJsonSafely(String json, Class<T> clazz, T defaultValue) {
        try {
            return fromJson(json, clazz);
        } catch (Exception e) {
            logger.warn("JSON转对象失败，返回默认值", e);
            return defaultValue;
        }
    }
}