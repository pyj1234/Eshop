package com.cat.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/api/*")
public class SecurityFilter implements Filter {
    
    // 不需要认证的路径
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/customers/register",
        "/api/customers/login",
        "/api/products",
        "/api/products/featured",
        "/api/products/search",
        "/api/products/categories"
    );
    
    // 不需要认证的路径前缀
    private static final List<String> PUBLIC_PATH_PREFIXES = Arrays.asList(
        "/api/products/"
    );
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化代码
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        if (contextPath != null) {
            path = path.substring(contextPath.length());
        }
        
        // 检查是否为公共路径
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 检查用户是否已登录
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("customerId") == null) {
            // 用户未登录
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(
                "{\"success\":false,\"message\":\"用户未登录\",\"errorCode\":\"UNAUTHORIZED\"}"
            );
            return;
        }
        
        // 检查管理员权限的路径
        if (path.startsWith("/api/admin/")) {
            String userType = (String) session.getAttribute("userType");
            if (!"ADMIN".equals(userType)) {
                // 权限不足
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write(
                    "{\"success\":false,\"message\":\"权限不足\",\"errorCode\":\"FORBIDDEN\"}"
                );
                return;
            }
        }
        
        // 通过所有检查，继续处理请求
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        // 清理资源
    }
    
    /**
     * 检查路径是否为公共路径（不需要认证）
     */
    private boolean isPublicPath(String path) {
        // 检查完全匹配的路径
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }
        
        // 检查路径前缀
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                // 排除需要认证的产品管理接口
                if (!path.startsWith("/api/products/") || 
                    (!path.contains("/stock") && 
                     !path.matches("/api/products/\\d+"))) {
                    return true;
                }
            }
        }
        
        return false;
    }
}