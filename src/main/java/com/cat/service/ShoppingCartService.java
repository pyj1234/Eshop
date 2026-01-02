package com.cat.service;

import com.cat.dao.ProductDAO;
import com.cat.dao.ShoppingCartDAO;
import com.cat.model.Product;
import com.cat.model.ShoppingCart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class ShoppingCartService {
    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartService.class);
    private final ShoppingCartDAO shoppingCartDAO;
    private final ProductDAO productDAO;
    
    public ShoppingCartService() {
        this.shoppingCartDAO = new ShoppingCartDAO();
        this.productDAO = new ProductDAO();
    }
    
    /**
     * 添加商品到购物车
     * @param customerId 客户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @return 操作结果
     */
    public OperationResult addToCart(Long customerId, Long productId, int quantity) {
        // 验证参数
        if (customerId == null || productId == null || quantity <= 0) {
            return new OperationResult(false, "参数无效");
        }
        
        // 检查商品是否存在
        Product product = productDAO.findById(productId);
        if (product == null || !product.isActive()) {
            return new OperationResult(false, "商品不存在或已下架");
        }
        
        // 检查库存
        if (!product.isInStock()) {
            return new OperationResult(false, "商品暂时缺货");
        }
        
        if (quantity > product.getStockQuantity()) {
            return new OperationResult(false, "库存不足，当前库存: " + product.getStockQuantity());
        }
        
        try {
            // 检查购物车中是否已有该商品
            ShoppingCart existingItem = shoppingCartDAO.findByCustomerAndProduct(customerId, productId);
            
            if (existingItem != null) {
                // 更新现有商品数量
                int newQuantity = existingItem.getQuantity() + quantity;
                
                if (newQuantity > product.getStockQuantity()) {
                    return new OperationResult(false, "库存不足，当前库存: " + product.getStockQuantity());
                }
                
                existingItem.setQuantity(newQuantity);
                boolean success = shoppingCartDAO.update(existingItem);
                
                if (success) {
                    logger.info("更新购物车商品数量成功，客户ID: {}, 商品ID: {}, 新数量: {}", 
                            customerId, productId, newQuantity);
                    return new OperationResult(true, "商品数量已更新");
                } else {
                    return new OperationResult(false, "更新商品数量失败");
                }
            } else {
                // 添加新商品到购物车
                ShoppingCart shoppingCart = new ShoppingCart(customerId, productId, quantity);
                Long cartItemId = shoppingCartDAO.add(shoppingCart);
                
                if (cartItemId != null) {
                    logger.info("添加商品到购物车成功，客户ID: {}, 商品ID: {}, 数量: {}", 
                            customerId, productId, quantity);
                    return new OperationResult(true, "商品已添加到购物车");
                } else {
                    return new OperationResult(false, "添加商品到购物车失败");
                }
            }
            
        } catch (Exception e) {
            logger.error("添加商品到购物车失败，客户ID: {}, 商品ID: {}, 数量: {}", 
                    customerId, productId, quantity, e);
            return new OperationResult(false, "操作失败，请稍后重试");
        }
    }
    
    /**
     * 更新购物车商品数量
     * @param customerId 客户ID
     * @param productId 商品ID
     * @param quantity 新数量
     * @return 操作结果
     */
    public OperationResult updateQuantity(Long customerId, Long productId, int quantity) {
        // 验证参数
        if (customerId == null || productId == null || quantity < 0) {
            return new OperationResult(false, "参数无效");
        }
        
        // 检查购物车项是否存在
        ShoppingCart cartItem = shoppingCartDAO.findByCustomerAndProduct(customerId, productId);
        if (cartItem == null) {
            return new OperationResult(false, "购物车中不存在该商品");
        }
        
        // 如果数量为0，则删除该商品
        if (quantity == 0) {
            return removeFromCart(customerId, productId);
        }
        
        // 检查商品库存
        Product product = productDAO.findById(productId);
        if (product == null || !product.isActive()) {
            return new OperationResult(false, "商品不存在或已下架");
        }
        
        if (quantity > product.getStockQuantity()) {
            return new OperationResult(false, "库存不足，当前库存: " + product.getStockQuantity());
        }
        
        try {
            cartItem.setQuantity(quantity);
            boolean success = shoppingCartDAO.update(cartItem);
            
            if (success) {
                logger.info("更新购物车商品数量成功，客户ID: {}, 商品ID: {}, 新数量: {}", 
                        customerId, productId, quantity);
                return new OperationResult(true, "商品数量已更新");
            } else {
                return new OperationResult(false, "更新商品数量失败");
            }
            
        } catch (Exception e) {
            logger.error("更新购物车商品数量失败，客户ID: {}, 商品ID: {}, 数量: {}", 
                    customerId, productId, quantity, e);
            return new OperationResult(false, "操作失败，请稍后重试");
        }
    }
    
    /**
     * 从购物车中移除商品
     * @param customerId 客户ID
     * @param productId 商品ID
     * @return 操作结果
     */
    public OperationResult removeFromCart(Long customerId, Long productId) {
        // 验证参数
        if (customerId == null || productId == null) {
            return new OperationResult(false, "参数无效");
        }
        
        try {
            boolean success = shoppingCartDAO.remove(customerId, productId);
            
            if (success) {
                logger.info("从购物车移除商品成功，客户ID: {}, 商品ID: {}", customerId, productId);
                return new OperationResult(true, "商品已从购物车移除");
            } else {
                return new OperationResult(false, "购物车中不存在该商品");
            }
            
        } catch (Exception e) {
            logger.error("从购物车移除商品失败，客户ID: {}, 商品ID: {}", customerId, productId, e);
            return new OperationResult(false, "操作失败，请稍后重试");
        }
    }
    
    /**
     * 清空购物车
     * @param customerId 客户ID
     * @return 操作结果
     */
    public OperationResult clearCart(Long customerId) {
        // 验证参数
        if (customerId == null) {
            return new OperationResult(false, "参数无效");
        }
        
        try {
            boolean success = shoppingCartDAO.clear(customerId);
            
            if (success) {
                logger.info("清空购物车成功，客户ID: {}", customerId);
                return new OperationResult(true, "购物车已清空");
            } else {
                return new OperationResult(false, "购物车已经是空的");
            }
            
        } catch (Exception e) {
            logger.error("清空购物车失败，客户ID: {}", customerId, e);
            return new OperationResult(false, "操作失败，请稍后重试");
        }
    }
    
    /**
     * 获取客户购物车
     * @param customerId 客户ID
     * @return 购物车项列表（包含商品信息）
     */
    public ShoppingCartResult getCustomerCart(Long customerId) {
        // 验证参数
        if (customerId == null) {
            return new ShoppingCartResult(null, 0, BigDecimal.ZERO, "参数无效");
        }
        
        try {
            List<ShoppingCart> cartItems = shoppingCartDAO.findByCustomerId(customerId);
            
            // 为每个购物车项加载商品信息
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalQuantity = 0;
            
            for (ShoppingCart item : cartItems) {
                Product product = productDAO.findById(item.getProductId());
                if (product != null && product.isActive()) {
                    item.setProduct(product);
                    totalAmount = totalAmount.add(item.getSubtotal());
                    totalQuantity += item.getQuantity();
                } else {
                    // 商品不存在或已下架，可以考虑从购物车中移除
                    logger.warn("购物车中的商品不存在或已下架，客户ID: {}, 商品ID: {}", 
                            customerId, item.getProductId());
                }
            }
            
            return new ShoppingCartResult(cartItems, totalQuantity, totalAmount, "获取成功");
            
        } catch (Exception e) {
            logger.error("获取客户购物车失败，客户ID: {}", customerId, e);
            return new ShoppingCartResult(null, 0, BigDecimal.ZERO, "获取购物车失败，请稍后重试");
        }
    }
    
    /**
     * 获取购物车商品数量
     * @param customerId 客户ID
     * @return 购物车商品总数量
     */
    public int getCartItemCount(Long customerId) {
        if (customerId == null) {
            return 0;
        }
        
        try {
            return shoppingCartDAO.getTotalQuantityByCustomerId(customerId);
        } catch (Exception e) {
            logger.error("获取购物车商品数量失败，客户ID: {}", customerId, e);
            return 0;
        }
    }
    
    /**
     * 检查购物车中所有商品是否都有足够库存
     * @param customerId 客户ID
     * @return 检查结果
     */
    public StockCheckResult checkCartStock(Long customerId) {
        List<ShoppingCart> cartItems = shoppingCartDAO.findByCustomerId(customerId);
        
        for (ShoppingCart item : cartItems) {
            Product product = productDAO.findById(item.getProductId());
            if (product == null || !product.isActive()) {
                return new StockCheckResult(false, "商品 " + product.getName() + " 不存在或已下架");
            }
            
            if (!product.isInStock()) {
                return new StockCheckResult(false, "商品 " + product.getName() + " 暂时缺货");
            }
            
            if (item.getQuantity() > product.getStockQuantity()) {
                return new StockCheckResult(false, "商品 " + product.getName() + " 库存不足，当前库存: " + product.getStockQuantity());
            }
        }
        
        return new StockCheckResult(true, "所有商品库存充足");
    }
    
    /**
     * 验证购物车商品数据的有效性
     * @param customerId 客户ID
     * @return 验证结果
     */
    public ValidationResult validateCart(Long customerId) {
        List<ShoppingCart> cartItems = shoppingCartDAO.findByCustomerId(customerId);
        
        for (ShoppingCart item : cartItems) {
            Product product = productDAO.findById(item.getProductId());
            if (product == null || !product.isActive()) {
                return new ValidationResult(false, "购物车中包含无效商品");
            }
            
            if (!product.isInStock()) {
                return new ValidationResult(false, "购物车中包含缺货商品");
            }
            
            if (item.getQuantity() > product.getStockQuantity()) {
                return new ValidationResult(false, "购物车中商品数量超过库存");
            }
        }
        
        return new ValidationResult(true, "购物车数据有效");
    }
    
    /**
     * 操作结果类
     */
    public static class OperationResult {
        private final boolean success;
        private final String message;
        
        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    /**
     * 购物车结果类
     */
    public static class ShoppingCartResult {
        private final List<ShoppingCart> cartItems;
        private final int totalQuantity;
        private final BigDecimal totalAmount;
        private final String message;
        
        public ShoppingCartResult(List<ShoppingCart> cartItems, int totalQuantity, BigDecimal totalAmount, String message) {
            this.cartItems = cartItems;
            this.totalQuantity = totalQuantity;
            this.totalAmount = totalAmount;
            this.message = message;
        }
        
        public List<ShoppingCart> getCartItems() { return cartItems; }
        public int getTotalQuantity() { return totalQuantity; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return cartItems != null; }
    }
    
    /**
     * 库存检查结果类
     */
    public static class StockCheckResult {
        private final boolean valid;
        private final String message;
        
        public StockCheckResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}