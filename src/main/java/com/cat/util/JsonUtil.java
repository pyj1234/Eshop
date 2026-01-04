package com.cat.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
// 【改动1】导入这个新模块
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();

        // 【改动2】这一行是关键！告诉 Jackson 开启对 LocalDateTime 的支持
        objectMapper.registerModule(new JavaTimeModule());

        // 禁用将日期写为时间戳（这样会输出 "2023-10-01T12:00:00" 而不是一串数字）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略空值
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    // 下面的代码保持不变...

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

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

    public static Map<String, Object> fromJsonToMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }

    public static <T> List<T> fromJsonToList(String json, Class<T> elementType) {
        try {
            CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            return objectMapper.readValue(json, collectionType);
        } catch (JsonProcessingException e) {
            logger.error("JSON转List失败: {}", json, e);
            throw new RuntimeException("JSON转List失败", e);
        }
    }

    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) {
            return null;
        }
        return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
    }

    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return objectMapper.convertValue(map, clazz);
    }

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

    public static String toJsonSafely(Object object, String defaultValue) {
        try {
            return toJson(object);
        } catch (Exception e) {
            logger.warn("对象转JSON失败，返回默认值", e);
            return defaultValue;
        }
    }

    public static <T> T fromJsonSafely(String json, Class<T> clazz, T defaultValue) {
        try {
            return fromJson(json, clazz);
        } catch (Exception e) {
            logger.warn("JSON转对象失败，返回默认值", e);
            return defaultValue;
        }
    }
}