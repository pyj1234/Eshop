package com.cat.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestUtil {
    
    /**
     * 获取客户端IP地址
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // 如果是多个IP地址，取第一个
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
    
    /**
     * 获取User-Agent
     * @param request HTTP请求对象
     * @return User-Agent字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
    
    /**
     * 获取请求体内容
     * @param request HTTP请求对象
     * @return 请求体内容
     * @throws IOException IO异常
     */
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        
        try {
            bufferedReader = request.getReader();
            char[] charBuffer = new char[128];
            int bytesRead;
            
            while ((bytesRead = bufferedReader.read(charBuffer)) != -1) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
        
        return stringBuilder.toString();
    }
    
    /**
     * 获取请求参数Map
     * @param request HTTP请求对象
     * @return 参数Map
     */
    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap<>();
        
        Map<String, String[]> parameterMapArray = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMapArray.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            
            if (values != null && values.length > 0) {
                // 如果参数有多个值，只取第一个
                parameterMap.put(key, values[0]);
            }
        }
        
        return parameterMap;
    }
    
    /**
     * 获取请求头Map
     * @param request HTTP请求对象
     * @return 请求头Map
     */
    public static Map<String, String> getHeaderMap(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        
        return headerMap;
    }
    
    /**
     * 检查是否为AJAX请求
     * @param request HTTP请求对象
     * @return 是否为AJAX请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");
        
        return (accept != null && accept.contains("application/json")) ||
               (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest"));
    }
    
    /**
     * 检查是否为GET请求
     * @param request HTTP请求对象
     * @return 是否为GET请求
     */
    public static boolean isGetRequest(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod());
    }
    
    /**
     * 检查是否为POST请求
     * @param request HTTP请求对象
     * @return 是否为POST请求
     */
    public static boolean isPostRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }
    
    /**
     * 检查是否为PUT请求
     * @param request HTTP请求对象
     * @return 是否为PUT请求
     */
    public static boolean isPutRequest(HttpServletRequest request) {
        return "PUT".equalsIgnoreCase(request.getMethod());
    }
    
    /**
     * 检查是否为DELETE请求
     * @param request HTTP请求对象
     * @return 是否为DELETE请求
     */
    public static boolean isDeleteRequest(HttpServletRequest request) {
        return "DELETE".equalsIgnoreCase(request.getMethod());
    }
    
    /**
     * 获取完整的请求URL
     * @param request HTTP请求对象
     * @return 完整的请求URL
     */
    public static String getFullUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        
        if (queryString != null && !queryString.isEmpty()) {
            url.append("?").append(queryString);
        }
        
        return url.toString();
    }
    
    /**
     * 获取上下文路径
     * @param request HTTP请求对象
     * @return 上下文路径
     */
    public static String getContextPath(HttpServletRequest request) {
        return request.getContextPath();
    }
    
    /**
     * 获取应用基础URL
     * @param request HTTP请求对象
     * @return 应用基础URL
     */
    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        
        // 只有在非标准端口时才包含端口号
        if ((!"http".equals(scheme) || serverPort != 80) && 
            (!"https".equals(scheme) || serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        
        baseUrl.append(contextPath);
        
        return baseUrl.toString();
    }
    
    /**
     * 验证参数是否为空或null
     * @param value 参数值
     * @return 是否为空
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * 验证参数是否不为空且不为null
     * @param value 参数值
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }
    
    /**
     * 获取参数值，如果为空则返回默认值
     * @param request HTTP请求对象
     * @param parameterName 参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static String getParameter(HttpServletRequest request, String parameterName, String defaultValue) {
        String value = request.getParameter(parameterName);
        return isEmpty(value) ? defaultValue : value.trim();
    }
    
    /**
     * 获取整数参数，如果无效则返回默认值
     * @param request HTTP请求对象
     * @param parameterName 参数名
     * @param defaultValue 默认值
     * @return 整数参数值
     */
    public static int getIntParameter(HttpServletRequest request, String parameterName, int defaultValue) {
        String value = request.getParameter(parameterName);
        if (isEmpty(value)) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取长整数参数，如果无效则返回默认值
     * @param request HTTP请求对象
     * @param parameterName 参数名
     * @param defaultValue 默认值
     * @return 长整数参数值
     */
    public static long getLongParameter(HttpServletRequest request, String parameterName, long defaultValue) {
        String value = request.getParameter(parameterName);
        if (isEmpty(value)) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取双精度浮点数参数，如果无效则返回默认值
     * @param request HTTP请求对象
     * @param parameterName 参数名
     * @param defaultValue 默认值
     * @return 双精度浮点数参数值
     */
    public static double getDoubleParameter(HttpServletRequest request, String parameterName, double defaultValue) {
        String value = request.getParameter(parameterName);
        if (isEmpty(value)) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔参数
     * @param request HTTP请求对象
     * @param parameterName 参数名
     * @param defaultValue 默认值
     * @return 布尔参数值
     */
    public static boolean getBooleanParameter(HttpServletRequest request, String parameterName, boolean defaultValue) {
        String value = request.getParameter(parameterName);
        if (isEmpty(value)) {
            return defaultValue;
        }
        
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim()) || "yes".equalsIgnoreCase(value.trim());
    }
}