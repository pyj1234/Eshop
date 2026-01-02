package com.cat.service;

import com.cat.dao.CustomerDAO;
import com.cat.model.Customer;
import com.cat.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerDAO customerDAO;
    
    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }
    
    /**
     * 用户注册
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @param firstName 名
     * @param lastName 姓
     * @param phone 电话
     * @return 注册结果
     */
    public RegistrationResult register(String username, String email, String password, 
                                    String firstName, String lastName, String phone) {
        
        // 验证输入参数
        String validationError = validateRegistrationInput(username, email, password, firstName, lastName);
        if (validationError != null) {
            return new RegistrationResult(false, validationError, null);
        }
        
        try {
            // 检查用户名是否已存在
            if (customerDAO.existsByUsername(username)) {
                return new RegistrationResult(false, "用户名已存在", null);
            }
            
            // 检查邮箱是否已存在
            if (customerDAO.existsByEmail(email)) {
                return new RegistrationResult(false, "邮箱已被注册", null);
            }
            
            // 创建客户对象
            Customer customer = new Customer();
            customer.setUsername(username.trim());
            customer.setEmail(email.trim().toLowerCase());
            customer.setPasswordHash(PasswordUtil.hashPassword(password));
            customer.setFirstName(firstName.trim());
            customer.setLastName(lastName.trim());
            customer.setPhone(phone != null ? phone.trim() : null);
            
            // 保存到数据库
            Long customerId = customerDAO.create(customer);
            customer.setId(customerId);
            
            logger.info("用户注册成功，ID: {}, 用户名: {}, 邮箱: {}", customerId, username, email);
            return new RegistrationResult(true, "注册成功", customer);
            
        } catch (Exception e) {
            logger.error("用户注册失败，用户名: {}, 邮箱: {}", username, email, e);
            return new RegistrationResult(false, "注册失败，请稍后重试", null);
        }
    }
    
    /**
     * 用户登录
     * @param username 用户名或邮箱
     * @param password 密码
     * @return 登录结果
     */
    public LoginResult login(String username, String password) {
        
        // 验证输入参数
        if (username == null || username.trim().isEmpty()) {
            return new LoginResult(false, "用户名或邮箱不能为空", null);
        }
        
        if (password == null || password.isEmpty()) {
            return new LoginResult(false, "密码不能为空", null);
        }
        
        try {
            // 查找用户
            Customer customer = customerDAO.authenticate(username.trim(), password);
            if (customer == null) {
                return new LoginResult(false, "用户名或密码错误", null);
            }
            
            // 验证密码
            if (!PasswordUtil.verifyPassword(password, customer.getPasswordHash())) {
                return new LoginResult(false, "用户名或密码错误", null);
            }
            
            // 检查账户是否激活
            if (!customer.isActive()) {
                return new LoginResult(false, "账户已被禁用，请联系管理员", null);
            }
            
            logger.info("用户登录成功，ID: {}, 用户名: {}", customer.getId(), customer.getUsername());
            return new LoginResult(true, "登录成功", customer);
            
        } catch (Exception e) {
            logger.error("用户登录失败，用户名: {}", username, e);
            return new LoginResult(false, "登录失败，请稍后重试", null);
        }
    }
    
    /**
     * 获取客户信息
     * @param customerId 客户ID
     * @return 客户对象
     */
    public Customer getCustomerById(Long customerId) {
        try {
            return customerDAO.findById(customerId);
        } catch (Exception e) {
            logger.error("获取客户信息失败，ID: {}", customerId, e);
            return null;
        }
    }
    
    /**
     * 更新客户信息
     * @param customer 客户对象
     * @return 更新结果
     */
    public UpdateResult updateCustomer(Customer customer) {
        // 验证输入参数
        String validationError = validateUpdateInput(customer);
        if (validationError != null) {
            return new UpdateResult(false, validationError);
        }
        
        try {
            // 检查用户名是否被其他用户占用
            Customer existingCustomer = customerDAO.findByUsername(customer.getUsername());
            if (existingCustomer != null && !existingCustomer.getId().equals(customer.getId())) {
                return new UpdateResult(false, "用户名已被其他用户使用");
            }
            
            // 检查邮箱是否被其他用户占用
            existingCustomer = customerDAO.findByEmail(customer.getEmail());
            if (existingCustomer != null && !existingCustomer.getId().equals(customer.getId())) {
                return new UpdateResult(false, "邮箱已被其他用户使用");
            }
            
            // 更新客户信息
            boolean success = customerDAO.update(customer);
            
            if (success) {
                logger.info("客户信息更新成功，ID: {}", customer.getId());
                return new UpdateResult(true, "更新成功");
            } else {
                return new UpdateResult(false, "更新失败，未找到客户信息");
            }
            
        } catch (Exception e) {
            logger.error("更新客户信息失败，ID: {}", customer.getId(), e);
            return new UpdateResult(false, "更新失败，请稍后重试");
        }
    }
    
    /**
     * 修改密码
     * @param customerId 客户ID
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    public UpdateResult changePassword(Long customerId, String currentPassword, String newPassword) {
        // 验证输入参数
        if (currentPassword == null || currentPassword.isEmpty()) {
            return new UpdateResult(false, "当前密码不能为空");
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            return new UpdateResult(false, "新密码不能为空");
        }
        
        if (!PasswordUtil.isValidPassword(newPassword)) {
            return new UpdateResult(false, "新密码长度不能少于6位");
        }
        
        try {
            // 获取客户信息
            Customer customer = customerDAO.findById(customerId);
            if (customer == null) {
                return new UpdateResult(false, "客户不存在");
            }
            
            // 验证当前密码
            if (!PasswordUtil.verifyPassword(currentPassword, customer.getPasswordHash())) {
                return new UpdateResult(false, "当前密码错误");
            }
            
            // 更新密码
            String newPasswordHash = PasswordUtil.hashPassword(newPassword);
            boolean success = customerDAO.updatePassword(customerId, newPasswordHash);
            
            if (success) {
                logger.info("密码修改成功，客户ID: {}", customerId);
                return new UpdateResult(true, "密码修改成功");
            } else {
                return new UpdateResult(false, "密码修改失败");
            }
            
        } catch (Exception e) {
            logger.error("修改密码失败，客户ID: {}", customerId, e);
            return new UpdateResult(false, "修改密码失败，请稍后重试");
        }
    }
    
    /**
     * 获取客户列表
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 客户列表
     */
    public List<Customer> getCustomerList(int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            return customerDAO.findAllActive(pageSize, offset);
        } catch (Exception e) {
            logger.error("获取客户列表失败，页码: {}, 每页大小: {}", page, pageSize, e);
            throw new RuntimeException("获取客户列表失败", e);
        }
    }
    
    /**
     * 获取客户总数
     * @return 总数
     */
    public long getCustomerCount() {
        try {
            return customerDAO.countActive();
        } catch (Exception e) {
            logger.error("获取客户总数失败", e);
            throw new RuntimeException("获取客户总数失败", e);
        }
    }
    
    /**
     * 搜索客户
     * @param keyword 搜索关键词
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 客户列表
     */
    public List<Customer> searchCustomers(String keyword, int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            return customerDAO.searchCustomers(keyword, pageSize, offset);
        } catch (Exception e) {
            logger.error("搜索客户失败，关键词: {}, 页码: {}, 每页大小: {}", keyword, page, pageSize, e);
            throw new RuntimeException("搜索客户失败", e);
        }
    }
    
    /**
     * 搜索客户总数
     * @param keyword 搜索关键词
     * @return 总数
     */
    public long getSearchCustomerCount(String keyword) {
        try {
            return customerDAO.countSearchCustomers(keyword);
        } catch (Exception e) {
            logger.error("获取搜索客户总数失败，关键词: {}", keyword, e);
            throw new RuntimeException("获取搜索客户总数失败", e);
        }
    }
    
    /**
     * 验证注册输入参数
     */
    private String validateRegistrationInput(String username, String email, String password, 
                                           String firstName, String lastName) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        
        if (username.length() < 3 || username.length() > 50) {
            return "用户名长度必须在3-50个字符之间";
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "用户名只能包含字母、数字和下划线";
        }
        
        if (email == null || email.trim().isEmpty()) {
            return "邮箱不能为空";
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            return "邮箱格式不正确";
        }
        
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }
        
        if (!PasswordUtil.isValidPassword(password)) {
            return "密码长度不能少于6位";
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            return "名字不能为空";
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            return "姓氏不能为空";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证更新输入参数
     */
    private String validateUpdateInput(Customer customer) {
        if (customer == null || customer.getId() == null) {
            return "客户信息不完整";
        }
        
        if (customer.getUsername() == null || customer.getUsername().trim().isEmpty()) {
            return "用户名不能为空";
        }
        
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            return "邮箱不能为空";
        }
        
        if (!customer.getEmail().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            return "邮箱格式不正确";
        }
        
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            return "名字不能为空";
        }
        
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            return "姓氏不能为空";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 注册结果类
     */
    public static class RegistrationResult {
        private final boolean success;
        private final String message;
        private final Customer customer;
        
        public RegistrationResult(boolean success, String message, Customer customer) {
            this.success = success;
            this.message = message;
            this.customer = customer;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Customer getCustomer() { return customer; }
    }
    
    /**
     * 登录结果类
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final Customer customer;
        
        public LoginResult(boolean success, String message, Customer customer) {
            this.success = success;
            this.message = message;
            this.customer = customer;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Customer getCustomer() { return customer; }
    }
    
    /**
     * 更新结果类
     */
    public static class UpdateResult {
        private final boolean success;
        private final String message;
        
        public UpdateResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}