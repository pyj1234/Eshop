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

        // 【关键修改】优先放行所有 OPTIONS 请求 (CORS 预检)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 【关键修改】如果是 GET 请求，且访问的是商品相关接口，直接放行
        // 这样 /api/products, /api/products/1, /api/products/search 统统不需要登录
        if ("GET".equalsIgnoreCase(httpRequest.getMethod()) && path.startsWith("/api/products")) {
            chain.doFilter(request, response);
            return;
        }

        // 检查公共路径 (保留之前的注册登录接口)
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
        return PUBLIC_PATHS.contains(path);
    }
}