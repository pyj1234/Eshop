package com.cat.servlet;

import com.cat.dto.ApiResponse;
import com.cat.model.ShoppingCart;
import com.cat.service.ShoppingCartService;
import com.cat.util.JsonUtil;
import com.cat.util.RequestUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/cart/*")
public class ShoppingCartServlet extends HttpServlet {
    private final ShoppingCartService shoppingCartService = new ShoppingCartService();
    
    private void writeJsonResponse(HttpServletResponse response, Object obj) throws IOException {
        response.getWriter().write(JsonUtil.toJson(obj));
    }
    
    /**
     * 检查用户是否登录并获取客户ID
     */
    private Long getAuthenticatedCustomerId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJsonResponse(response, ApiResponse.error("用户未登录"));
            return null;
        }
        return (Long) session.getAttribute("customerId");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetCart(request, response);
            } else if (pathInfo.equals("/count")) {
                handleGetCartCount(request, response);
            } else if (pathInfo.equals("/validate")) {
                handleValidateCart(request, response);
            } else if (pathInfo.equals("/stock-check")) {
                handleStockCheck(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                handleAddToCart(request, response);
            } else if (pathInfo.equals("/clear")) {
                handleClearCart(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                handleUpdateQuantity(request, response, pathInfo);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                handleRemoveFromCart(request, response, pathInfo);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    /**
     * 处理获取购物车
     */
    private void handleGetCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = getAuthenticatedCustomerId(request, response);
        if (customerId == null) return;
        
        ShoppingCartService.ShoppingCartResult cartResult = shoppingCartService.getCustomerCart(customerId);
        
        if (cartResult.isSuccess()) {
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("cartItems", cartResult.getCartItems());
            responseData.put("totalQuantity", cartResult.getTotalQuantity());
            responseData.put("totalAmount", cartResult.getTotalAmount());
            
            ApiResponse<Map<String, Object>> responseObj = ApiResponse.success(cartResult.getMessage(), responseData);
            writeJsonResponse(response, responseObj);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error(cartResult.getMessage()));
        }
    }
    
    /**
     * 处理获取购物车商品数量
     */
    private void handleGetCartCount(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = getAuthenticatedCustomerId(request, response);
        if (customerId == null) return;
        
        int itemCount = shoppingCartService.getCartItemCount(customerId);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("itemCount", itemCount);
        
        ApiResponse<Map<String, Object>> responseObj = ApiResponse.success("获取成功", responseData);
        writeJsonResponse(response, responseObj);
    }
    
    /**
     * 处理添加商品到购物车
     */
    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = getAuthenticatedCustomerId(request, response);
        if (customerId == null) return;
        
        try {
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            Object productIdObj = requestData.get("productId");
            Object quantityObj = requestData.get("quantity");
            
            if (productIdObj == null || quantityObj == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error("商品ID和数量不能为空"));
                return;
            }
            
            Long productId = ((Number) productIdObj).longValue();
            int quantity = ((Number) quantityObj).intValue();
            
            ShoppingCartService.OperationResult result = shoppingCartService.addToCart(customerId, productId, quantity);
            
            if (result.isSuccess()) {
                // 返回更新后的购物车信息
                ShoppingCartService.ShoppingCartResult cartResult = shoppingCartService.getCustomerCart(customerId);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("cartItems", cartResult.getCartItems());
                responseData.put("totalQuantity", cartResult.getTotalQuantity());
                responseData.put("totalAmount", cartResult.getTotalAmount());
                
                ApiResponse<Map<String, Object>> responseObj = ApiResponse.success(result.getMessage(), responseData);
                writeJsonResponse(response, responseObj);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数格式错误"));
        }
    }
    
    /**
     * 处理更新购物车商品数量
     */
    private void handleUpdateQuantity(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        Long customerId = getAuthenticatedCustomerId(request, response);
        if (customerId == null) return;
        
        try {
            // 从路径中提取商品ID
            Long productId = Long.parseLong(pathInfo.substring(1));
            
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            Object quantityObj = requestData.get("quantity");
            if (quantityObj == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error("数量不能为空"));
                return;
            }
            
            int quantity = ((Number) quantityObj).intValue();
            
            ShoppingCartService.OperationResult result = shoppingCartService.updateQuantity(customerId, productId, quantity);
            
            if (result.isSuccess()) {
                // 返回更新后的购物车信息
                ShoppingCartService.ShoppingCartResult cartResult = shoppingCartService.getCustomerCart(customerId);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("cartItems", cartResult.getCartItems());
                responseData.put("totalQuantity", cartResult.getTotalQuantity());
                responseData.put("totalAmount", cartResult.getTotalAmount());
                
                ApiResponse<Map<String, Object>> responseObj = ApiResponse.success(result.getMessage(), responseData);
                writeJsonResponse(response, responseObj);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("商品ID格式错误"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数格式错误"));
        }
    }
    
    /**
     * 处理从购物车移除商品
     */
    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        Long customerId = getAuthenticatedCustomerId(request, response);
        if (customerId == null) return;
        
        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            
            ShoppingCartService.OperationResult result = shoppingCartService.removeFromCart(customerId, productId);
            
            if (result.isSuccess()) {
                // 返回更新后的购物车信息
                ShoppingCartService.ShoppingCartResult cartResult = shoppingCartService.getCustomerCart(customerId);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("cartItems", cartResult.getCartItems());
                responseData.put("totalQuantity", cartResult.getTotalQuantity());
                responseData.put("totalAmount", cartResult.getTotalAmount());
                
                ApiResponse<Map<String, Object>> responseObj = ApiResponse.success(result.getMessage(), responseData);
                writeJsonResponse(response, responseObj);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("商品ID格式错误"));
        }
    }
    
    /**
     * 处理清空购物车
     */
    private void handleClearCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = getAuthenticatedCustomerId(request, response);
        if (customerId == null) return;
        
        ShoppingCartService.OperationResult result = shoppingCartService.clearCart(customerId);
        
        if (result.isSuccess()) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("cartItems", new java.util.ArrayList<>());
            responseData.put("totalQuantity", 0);
            responseData.put("totalAmount", java.math.BigDecimal.ZERO);
            
            ApiResponse<Map<String, Object>> responseObj = ApiResponse.success(result.getMessage(), responseData);
            writeJsonResponse(response, responseObj);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error(result.getMessage()));
        }
    }
    
    /**
     * 处理验证购物车
     */
    private void handleValidateCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = getAuthenticatedCustomerId(request, response);
        if (customerId == null) return;
        
        ShoppingCartService.ValidationResult result = shoppingCartService.validateCart(customerId);
        
        if (result.isValid()) {
            writeJsonResponse(response, ApiResponse.success(result.getMessage()));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error(result.getMessage()));
        }
    }
    
    /**
     * 处理购物车库存检查
     */
    private void handleStockCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = getAuthenticatedCustomerId(request, response);
        if (customerId == null) return;
        
        ShoppingCartService.StockCheckResult result = shoppingCartService.checkCartStock(customerId);
        
        if (result.isValid()) {
            writeJsonResponse(response, ApiResponse.success(result.getMessage()));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error(result.getMessage()));
        }
    }
}