package com.cat.dao;

import com.cat.model.ShoppingCart;
import com.cat.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ShoppingCartDAO {
    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartDAO.class);
    
    /**
     * 添加商品到购物车
     * @param shoppingCart 购物车项
     * @return 创建的购物车项ID，如果商品已存在则返回null
     */
    public Long add(ShoppingCart shoppingCart) {
        // 检查是否已存在相同的商品
        ShoppingCart existing = findByCustomerAndProduct(shoppingCart.getCustomerId(), shoppingCart.getProductId());
        if (existing != null) {
            return null; // 商品已存在，应该更新数量而不是创建新的
        }
        
        String sql = "INSERT INTO shopping_cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
        
        try {
            long id = DatabaseUtil.executeInsert(sql, 
                    shoppingCart.getCustomerId(),
                    shoppingCart.getProductId(),
                    shoppingCart.getQuantity());
            
            logger.info("成功添加商品到购物车，ID: {}, 客户ID: {}, 商品ID: {}", 
                    id, shoppingCart.getCustomerId(), shoppingCart.getProductId());
            return id;
            
        } catch (Exception e) {
            logger.error("添加商品到购物车失败，客户ID: {}, 商品ID: {}", 
                    shoppingCart.getCustomerId(), shoppingCart.getProductId(), e);
            throw new RuntimeException("添加商品到购物车失败", e);
        }
    }
    
    /**
     * 更新购物车商品数量
     * @param shoppingCart 购物车项
     * @return 是否成功
     */
    public boolean update(ShoppingCart shoppingCart) {
        String sql = "UPDATE shopping_cart SET quantity = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE customer_id = ? AND product_id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql,
                    shoppingCart.getQuantity(),
                    shoppingCart.getCustomerId(),
                    shoppingCart.getProductId());
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功更新购物车商品数量，客户ID: {}, 商品ID: {}, 新数量: {}", 
                        shoppingCart.getCustomerId(), shoppingCart.getProductId(), shoppingCart.getQuantity());
            } else {
                logger.warn("更新购物车商品数量失败，未找到记录，客户ID: {}, 商品ID: {}", 
                        shoppingCart.getCustomerId(), shoppingCart.getProductId());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("更新购物车商品数量失败，客户ID: {}, 商品ID: {}", 
                    shoppingCart.getCustomerId(), shoppingCart.getProductId(), e);
            throw new RuntimeException("更新购物车商品数量失败", e);
        }
    }
    
    /**
     * 根据ID查找购物车项
     * @param id 购物车项ID
     * @return 购物车项，如果不存在返回null
     */
    public ShoppingCart findById(Long id) {
        String sql = "SELECT id, customer_id, product_id, quantity, created_at, updated_at " +
                     "FROM shopping_cart WHERE id = ?";
        
        return DatabaseUtil.querySingle(sql, this::mapResultSetToShoppingCart, id);
    }
    
    /**
     * 根据客户ID和商品ID查找购物车项
     * @param customerId 客户ID
     * @param productId 商品ID
     * @return 购物车项，如果不存在返回null
     */
    public ShoppingCart findByCustomerAndProduct(Long customerId, Long productId) {
        String sql = "SELECT id, customer_id, product_id, quantity, created_at, updated_at " +
                     "FROM shopping_cart WHERE customer_id = ? AND product_id = ?";
        
        return DatabaseUtil.querySingle(sql, this::mapResultSetToShoppingCart, customerId, productId);
    }
    
    /**
     * 获取客户的所有购物车项
     * @param customerId 客户ID
     * @return 购物车项列表
     */
    public List<ShoppingCart> findByCustomerId(Long customerId) {
        String sql = "SELECT id, customer_id, product_id, quantity, created_at, updated_at " +
                     "FROM shopping_cart WHERE customer_id = ? ORDER BY created_at DESC";
        
        return DatabaseUtil.queryList(sql, this::mapResultSetToShoppingCart, customerId);
    }
    
    /**
     * 获取客户的购物车项数量
     * @param customerId 客户ID
     * @return 购物车项数量
     */
    public long countByCustomerId(Long customerId) {
        String sql = "SELECT COUNT(*) FROM shopping_cart WHERE customer_id = ?";
        return DatabaseUtil.count(sql, customerId);
    }
    
    /**
     * 获取客户的购物车商品总数量
     * @param customerId 客户ID
     * @return 商品总数量
     */
    public int getTotalQuantityByCustomerId(Long customerId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM shopping_cart WHERE customer_id = ?";
        
        try {
            return DatabaseUtil.querySingle(sql, (rs) -> rs.getInt(1), customerId);
        } catch (Exception e) {
            logger.error("获取客户购物车商品总数量失败，客户ID: {}", customerId, e);
            return 0;
        }
    }
    
    /**
     * 删除购物车项
     * @param customerId 客户ID
     * @param productId 商品ID
     * @return 是否成功
     */
    public boolean remove(Long customerId, Long productId) {
        String sql = "DELETE FROM shopping_cart WHERE customer_id = ? AND product_id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, customerId, productId);
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功删除购物车项，客户ID: {}, 商品ID: {}", customerId, productId);
            } else {
                logger.warn("删除购物车项失败，未找到记录，客户ID: {}, 商品ID: {}", customerId, productId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("删除购物车项失败，客户ID: {}, 商品ID: {}", customerId, productId, e);
            throw new RuntimeException("删除购物车项失败", e);
        }
    }
    
    /**
     * 根据ID删除购物车项
     * @param id 购物车项ID
     * @return 是否成功
     */
    public boolean removeById(Long id) {
        String sql = "DELETE FROM shopping_cart WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, id);
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功删除购物车项，ID: {}", id);
            } else {
                logger.warn("删除购物车项失败，未找到记录，ID: {}", id);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("删除购物车项失败，ID: {}", id, e);
            throw new RuntimeException("删除购物车项失败", e);
        }
    }
    
    /**
     * 清空客户购物车
     * @param customerId 客户ID
     * @return 是否成功
     */
    public boolean clear(Long customerId) {
        String sql = "DELETE FROM shopping_cart WHERE customer_id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, customerId);
            
            logger.info("成功清空客户购物车，客户ID: {}, 删除项数: {}", customerId, rows);
            return rows > 0;
            
        } catch (Exception e) {
            logger.error("清空客户购物车失败，客户ID: {}", customerId, e);
            throw new RuntimeException("清空客户购物车失败", e);
        }
    }
    
    /**
     * 检查客户购物车中是否包含指定商品
     * @param customerId 客户ID
     * @param productId 商品ID
     * @return 是否包含
     */
    public boolean containsProduct(Long customerId, Long productId) {
        String sql = "SELECT 1 FROM shopping_cart WHERE customer_id = ? AND product_id = ?";
        return DatabaseUtil.exists(sql, customerId, productId);
    }
    
    /**
     * 将ResultSet映射为ShoppingCart对象
     * @param rs ResultSet
     * @return ShoppingCart对象
     * @throws SQLException SQL异常
     */
    private ShoppingCart mapResultSetToShoppingCart(ResultSet rs) throws SQLException {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(rs.getLong("id"));
        shoppingCart.setCustomerId(rs.getLong("customer_id"));
        shoppingCart.setProductId(rs.getLong("product_id"));
        shoppingCart.setQuantity(rs.getInt("quantity"));
        shoppingCart.setCreatedAt(rs.getTimestamp("created_at") != null ? 
                rs.getTimestamp("created_at").toLocalDateTime() : null);
        shoppingCart.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
        
        return shoppingCart;
    }
}