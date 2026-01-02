package com.cat.servlet;

import com.cat.dto.ApiResponse;
import com.cat.model.Customer;
import com.cat.service.CustomerService;
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

@WebServlet("/api/customers/*")
public class CustomerServlet extends HttpServlet {
    private final CustomerService customerService = new CustomerService();
    
    private void writeJsonResponse(HttpServletResponse response, Object obj) throws IOException {
        response.getWriter().write(JsonUtil.toJson(obj));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetCustomers(request, response);
            } else if (pathInfo.startsWith("/profile")) {
                handleGetProfile(request, response);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetCustomerById(request, response, pathInfo);
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

            // 修改点：同时支持 "/" 和 "/register" 作为注册接口
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/register")) {
                handleRegister(request, response);
            } else if (pathInfo.equals("/login")) {
                handleLogin(request, response);
            } else if (pathInfo.equals("/logout")) {
                handleLogout(request, response);
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
            
            if (pathInfo != null && pathInfo.equals("/profile")) {
                handleUpdateProfile(request, response);
            } else if (pathInfo != null && pathInfo.equals("/password")) {
                handleChangePassword(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            String username = (String) requestData.get("username");
            String email = (String) requestData.get("email");
            String password = (String) requestData.get("password");
            String firstName = (String) requestData.get("firstName");
            String lastName = (String) requestData.get("lastName");
            String phone = (String) requestData.get("phone");
            
            CustomerService.RegistrationResult result = customerService.register(
                username, email, password, firstName, lastName, phone);
            
            if (result.isSuccess()) {
                ApiResponse<Customer> responseObj = ApiResponse.success("注册成功", result.getCustomer());
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
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            String username = (String) requestData.get("username");
            String password = (String) requestData.get("password");
            boolean rememberMe = Boolean.parseBoolean(String.valueOf(requestData.get("rememberMe")));
            
            CustomerService.LoginResult result = customerService.login(username, password);
            
            if (result.isSuccess()) {
                Customer customer = result.getCustomer();
                
                HttpSession session = request.getSession();
                session.setAttribute("customerId", customer.getId());
                session.setAttribute("username", customer.getUsername());
                session.setAttribute("userType", "CUSTOMER");
                
                if (rememberMe) {
                    session.setMaxInactiveInterval(30 * 24 * 60 * 60);
                } else {
                    session.setMaxInactiveInterval(30 * 60);
                }
                
                Map<String, Object> customerData = new HashMap<>();
                customerData.put("id", customer.getId());
                customerData.put("username", customer.getUsername());
                customerData.put("email", customer.getEmail());
                customerData.put("firstName", customer.getFirstName());
                customerData.put("lastName", customer.getLastName());
                customerData.put("phone", customer.getPhone());
                customerData.put("fullName", customer.getFullName());
                
                ApiResponse<Map<String, Object>> responseObj = ApiResponse.success("登录成功", customerData);
                writeJsonResponse(response, responseObj);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数格式错误"));
        }
    }
    
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        writeJsonResponse(response, ApiResponse.success("退出成功"));
    }
    
    private void handleGetProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJsonResponse(response, ApiResponse.error("用户未登录"));
            return;
        }
        
        Long customerId = (Long) session.getAttribute("customerId");
        Customer customer = customerService.getCustomerById(customerId);
        
        if (customer != null) {
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("id", customer.getId());
            customerData.put("username", customer.getUsername());
            customerData.put("email", customer.getEmail());
            customerData.put("firstName", customer.getFirstName());
            customerData.put("lastName", customer.getLastName());
            customerData.put("phone", customer.getPhone());
            customerData.put("fullName", customer.getFullName());
            customerData.put("createdAt", customer.getCreatedAt());
            
            ApiResponse<Map<String, Object>> responseObj = ApiResponse.success("获取成功", customerData);
            writeJsonResponse(response, responseObj);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJsonResponse(response, ApiResponse.error("用户不存在"));
        }
    }
    
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJsonResponse(response, ApiResponse.error("用户未登录"));
            return;
        }
        
        try {
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            Long customerId = (Long) session.getAttribute("customerId");
            
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setUsername((String) requestData.get("username"));
            customer.setEmail((String) requestData.get("email"));
            customer.setFirstName((String) requestData.get("firstName"));
            customer.setLastName((String) requestData.get("lastName"));
            customer.setPhone((String) requestData.get("phone"));
            
            CustomerService.UpdateResult result = customerService.updateCustomer(customer);
            
            if (result.isSuccess()) {
                writeJsonResponse(response, ApiResponse.success(result.getMessage()));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数格式错误"));
        }
    }
    
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJsonResponse(response, ApiResponse.error("用户未登录"));
            return;
        }
        
        try {
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            Long customerId = (Long) session.getAttribute("customerId");
            String currentPassword = (String) requestData.get("currentPassword");
            String newPassword = (String) requestData.get("newPassword");
            
            CustomerService.UpdateResult result = customerService.changePassword(
                customerId, currentPassword, newPassword);
            
            if (result.isSuccess()) {
                writeJsonResponse(response, ApiResponse.success(result.getMessage()));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数格式错误"));
        }
    }
    
    private void handleGetCustomers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("userType"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJsonResponse(response, ApiResponse.error("权限不足"));
            return;
        }
        
        try {
            String keyword = RequestUtil.getParameter(request, "keyword", "");
            int page = RequestUtil.getIntParameter(request, "page", 1);
            int pageSize = RequestUtil.getIntParameter(request, "pageSize", 10);
            
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            List<Customer> customers;
            long totalCount;
            
            if (keyword.isEmpty()) {
                customers = customerService.getCustomerList(page, pageSize);
                totalCount = customerService.getCustomerCount();
            } else {
                customers = customerService.searchCustomers(keyword, page, pageSize);
                totalCount = customerService.getSearchCustomerCount(keyword);
            }
            
            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("page", page);
            pageInfo.put("pageSize", pageSize);
            pageInfo.put("totalCount", totalCount);
            pageInfo.put("totalPages", (totalCount + pageSize - 1) / pageSize);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customers", customers);
            responseData.put("pageInfo", pageInfo);
            
            ApiResponse<Map<String, Object>> responseObj = ApiResponse.success("获取成功", responseData);
            writeJsonResponse(response, responseObj);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数错误"));
        }
    }
    
    private void handleGetCustomerById(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("userType"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJsonResponse(response, ApiResponse.error("权限不足"));
            return;
        }
        
        try {
            Long customerId = Long.parseLong(pathInfo.substring(1));
            Customer customer = customerService.getCustomerById(customerId);
            
            if (customer != null) {
                ApiResponse<Customer> responseObj = ApiResponse.success("获取成功", customer);
                writeJsonResponse(response, responseObj);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("客户不存在"));
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("客户ID格式错误"));
        }
    }
}