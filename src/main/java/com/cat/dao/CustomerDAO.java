package com.cat.dao;

import com.cat.model.Customer;
import com.cat.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CustomerDAO {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);
    
    /**
     * 创建新客户
     * @param customer 客户对象
     * @return 创建的客户ID
     */
    public Long create(Customer customer) {
        String sql = "INSERT INTO customers (username, email, password_hash, first_name, last_name, phone, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            long id = DatabaseUtil.executeInsert(sql, 
                    customer.getUsername(),
                    customer.getEmail(),
                    customer.getPasswordHash(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getPhone(),
                    customer.isActive());
            
            logger.info("成功创建客户，ID: {}, 用户名: {}", id, customer.getUsername());
            return id;
            
        } catch (Exception e) {
            logger.error("创建客户失败: {}", customer.getUsername(), e);
            throw new RuntimeException("创建客户失败", e);
        }
    }
    
    /**
     * 根据ID查找客户
     * @param id 客户ID
     * @return 客户对象，如果不存在返回null
     */
    public Customer findById(Long id) {
        String sql = "SELECT id, username, email, password_hash, first_name, last_name, phone, " +
                     "created_at, updated_at, is_active FROM customers WHERE id = ?";
        
        return DatabaseUtil.querySingle(sql, this::mapResultSetToCustomer, id);
    }
    
    /**
     * 根据用户名查找客户
     * @param username 用户名
     * @return 客户对象，如果不存在返回null
     */
    public Customer findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, first_name, last_name, phone, " +
                     "created_at, updated_at, is_active FROM customers WHERE username = ? AND is_active = true";
        
        return DatabaseUtil.querySingle(sql, this::mapResultSetToCustomer, username);
    }
    
    /**
     * 根据邮箱查找客户
     * @param email 邮箱
     * @return 客户对象，如果不存在返回null
     */
    public Customer findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash, first_name, last_name, phone, " +
                     "created_at, updated_at, is_active FROM customers WHERE email = ? AND is_active = true";
        
        return DatabaseUtil.querySingle(sql, this::mapResultSetToCustomer, email);
    }
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM customers WHERE username = ?";
        return DatabaseUtil.exists(sql, username);
    }
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM customers WHERE email = ?";
        return DatabaseUtil.exists(sql, email);
    }
    
    /**
     * 验证用户登录
     * @param username 用户名
     * @param password 密码
     * @return 客户对象，验证失败返回null
     */
    public Customer authenticate(String username, String password) {
        // 首先尝试通过用户名查找
        Customer customer = findByUsername(username);
        if (customer == null) {
            // 如果用户名不存在，尝试通过邮箱查找
            customer = findByEmail(username);
        }
        
        if (customer != null) {
            // 这里需要密码验证，在Service层进行
            return customer;
        }
        
        return null;
    }
    
    /**
     * 更新客户信息
     * @param customer 客户对象
     * @return 是否成功
     */
    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET username = ?, email = ?, first_name = ?, last_name = ?, " +
                     "phone = ?, is_active = ? WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql,
                    customer.getUsername(),
                    customer.getEmail(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getPhone(),
                    customer.isActive(),
                    customer.getId());
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功更新客户信息，ID: {}", customer.getId());
            } else {
                logger.warn("更新客户信息失败，未找到记录，ID: {}", customer.getId());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("更新客户信息失败，ID: {}", customer.getId(), e);
            throw new RuntimeException("更新客户信息失败", e);
        }
    }
    
    /**
     * 更新密码
     * @param customerId 客户ID
     * @param newPasswordHash 新密码哈希
     * @return 是否成功
     */
    public boolean updatePassword(Long customerId, String newPasswordHash) {
        String sql = "UPDATE customers SET password_hash = ? WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, newPasswordHash, customerId);
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功更新客户密码，ID: {}", customerId);
            } else {
                logger.warn("更新客户密码失败，未找到记录，ID: {}", customerId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("更新客户密码失败，ID: {}", customerId, e);
            throw new RuntimeException("更新客户密码失败", e);
        }
    }
    
    /**
     * 禁用客户账户
     * @param customerId 客户ID
     * @return 是否成功
     */
    public boolean deactivate(Long customerId) {
        String sql = "UPDATE customers SET is_active = false WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, customerId);
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功禁用客户账户，ID: {}", customerId);
            } else {
                logger.warn("禁用客户账户失败，未找到记录，ID: {}", customerId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("禁用客户账户失败，ID: {}", customerId, e);
            throw new RuntimeException("禁用客户账户失败", e);
        }
    }
    
    /**
     * 启用客户账户
     * @param customerId 客户ID
     * @return 是否成功
     */
    public boolean activate(Long customerId) {
        String sql = "UPDATE customers SET is_active = true WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, customerId);
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功启用客户账户，ID: {}", customerId);
            } else {
                logger.warn("启用客户账户失败，未找到记录，ID: {}", customerId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("启用客户账户失败，ID: {}", customerId, e);
            throw new RuntimeException("启用客户账户失败", e);
        }
    }
    
    /**
     * 获取所有活跃客户列表
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 客户列表
     */
    public List<Customer> findAllActive(int limit, int offset) {
        String sql = "SELECT id, username, email, password_hash, first_name, last_name, phone, " +
                     "created_at, updated_at, is_active FROM customers WHERE is_active = true " +
                     "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        return DatabaseUtil.queryList(sql, this::mapResultSetToCustomer, limit, offset);
    }
    
    /**
     * 获取活跃客户总数
     * @return 总数
     */
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM customers WHERE is_active = true";
        return DatabaseUtil.count(sql);
    }
    
    /**
     * 搜索客户
     * @param keyword 搜索关键词
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 客户列表
     */
    public List<Customer> searchCustomers(String keyword, int limit, int offset) {
        String sql = "SELECT id, username, email, password_hash, first_name, last_name, phone, " +
                     "created_at, updated_at, is_active FROM customers " +
                     "WHERE is_active = true AND (username LIKE ? OR email LIKE ? OR first_name LIKE ? OR last_name LIKE ?) " +
                     "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        String searchPattern = "%" + keyword + "%";
        return DatabaseUtil.queryList(sql, this::mapResultSetToCustomer, 
                searchPattern, searchPattern, searchPattern, searchPattern, limit, offset);
    }
    
    /**
     * 搜索客户总数
     * @param keyword 搜索关键词
     * @return 总数
     */
    public long countSearchCustomers(String keyword) {
        String sql = "SELECT COUNT(*) FROM customers " +
                     "WHERE is_active = true AND (username LIKE ? OR email LIKE ? OR first_name LIKE ? OR last_name LIKE ?)";
        
        String searchPattern = "%" + keyword + "%";
        return DatabaseUtil.count(sql, searchPattern, searchPattern, searchPattern, searchPattern);
    }
    
    /**
     * 将ResultSet映射为Customer对象
     * @param rs ResultSet
     * @return Customer对象
     * @throws SQLException SQL异常
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setUsername(rs.getString("username"));
        customer.setEmail(rs.getString("email"));
        customer.setPasswordHash(rs.getString("password_hash"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setCreatedAt(rs.getTimestamp("created_at") != null ? 
                rs.getTimestamp("created_at").toLocalDateTime() : null);
        customer.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
        customer.setActive(rs.getBoolean("is_active"));
        
        return customer;
    }
}