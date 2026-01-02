package com.cat.dao;

import com.cat.model.Product;
import com.cat.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);
    
    /**
     * 创建商品
     * @param product 商品对象
     * @return 创建的商品ID
     */
    public Long create(Product product) {
        String sql = "INSERT INTO products (name, description, short_description, sku, price, cost_price, " +
                     "stock_quantity, min_stock_level, category_id, image_url, images, weight, dimensions, " +
                     "is_active, is_featured) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            long id = DatabaseUtil.executeInsert(sql, 
                    product.getName(),
                    product.getDescription(),
                    product.getShortDescription(),
                    product.getSku(),
                    product.getPrice(),
                    product.getCostPrice(),
                    product.getStockQuantity(),
                    product.getMinStockLevel(),
                    product.getCategoryId(),
                    product.getImageUrl(),
                    product.getImages() != null ? java.util.Arrays.toString(product.getImages().toArray()) : null,
                    product.getWeight(),
                    product.getDimensions(),
                    product.isActive(),
                    product.isFeatured());
            
            logger.info("成功创建商品，ID: {}, 名称: {}, SKU: {}", id, product.getName(), product.getSku());
            return id;
            
        } catch (Exception e) {
            logger.error("创建商品失败: {}", product.getName(), e);
            throw new RuntimeException("创建商品失败", e);
        }
    }
    
    /**
     * 根据ID查找商品
     * @param id 商品ID
     * @return 商品对象，如果不存在返回null
     */
    public Product findById(Long id) {
        String sql = "SELECT id, name, description, short_description, sku, price, cost_price, " +
                     "stock_quantity, min_stock_level, category_id, image_url, images, weight, dimensions, " +
                     "is_active, is_featured, created_at, updated_at FROM products WHERE id = ?";
        
        return DatabaseUtil.querySingle(sql, this::mapResultSetToProduct, id);
    }
    
    /**
     * 根据SKU查找商品
     * @param sku SKU
     * @return 商品对象，如果不存在返回null
     */
    public Product findBySku(String sku) {
        String sql = "SELECT id, name, description, short_description, sku, price, cost_price, " +
                     "stock_quantity, min_stock_level, category_id, image_url, images, weight, dimensions, " +
                     "is_active, is_featured, created_at, updated_at FROM products WHERE sku = ?";
        
        return DatabaseUtil.querySingle(sql, this::mapResultSetToProduct, sku);
    }
    
    /**
     * 检查SKU是否存在
     * @param sku SKU
     * @return 是否存在
     */
    public boolean existsBySku(String sku) {
        String sql = "SELECT 1 FROM products WHERE sku = ?";
        return DatabaseUtil.exists(sql, sku);
    }
    
    /**
     * 更新商品信息
     * @param product 商品对象
     * @return 是否成功
     */
    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, short_description = ?, sku = ?, " +
                     "price = ?, cost_price = ?, stock_quantity = ?, min_stock_level = ?, category_id = ?, " +
                     "image_url = ?, images = ?, weight = ?, dimensions = ?, is_active = ?, is_featured = ? " +
                     "WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql,
                    product.getName(),
                    product.getDescription(),
                    product.getShortDescription(),
                    product.getSku(),
                    product.getPrice(),
                    product.getCostPrice(),
                    product.getStockQuantity(),
                    product.getMinStockLevel(),
                    product.getCategoryId(),
                    product.getImageUrl(),
                    product.getImages() != null ? 
                            java.util.Arrays.toString(product.getImages().toArray()) : null,
                    product.getWeight(),
                    product.getDimensions(),
                    product.isActive(),
                    product.isFeatured(),
                    product.getId());
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功更新商品信息，ID: {}", product.getId());
            } else {
                logger.warn("更新商品信息失败，未找到记录，ID: {}", product.getId());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("更新商品信息失败，ID: {}", product.getId(), e);
            throw new RuntimeException("更新商品信息失败", e);
        }
    }
    
    /**
     * 删除商品（逻辑删除）
     * @param id 商品ID
     * @return 是否成功
     */
    public boolean delete(Long id) {
        String sql = "UPDATE products SET is_active = false WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, id);
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功删除商品，ID: {}", id);
            } else {
                logger.warn("删除商品失败，未找到记录，ID: {}", id);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("删除商品失败，ID: {}", id, e);
            throw new RuntimeException("删除商品失败", e);
        }
    }
    
    /**
     * 获取所有活跃商品列表
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 商品列表
     */
    public List<Product> findAllActive(int limit, int offset) {
        String sql = "SELECT id, name, description, short_description, sku, price, cost_price, " +
                     "stock_quantity, min_stock_level, category_id, image_url, images, weight, dimensions, " +
                     "is_active, is_featured, created_at, updated_at FROM products WHERE is_active = true " +
                     "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        return DatabaseUtil.queryList(sql, this::mapResultSetToProduct, limit, offset);
    }
    
    /**
     * 根据分类获取商品列表
     * @param categoryId 分类ID
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 商品列表
     */
    public List<Product> findByCategory(Long categoryId, int limit, int offset) {
        String sql = "SELECT id, name, description, short_description, sku, price, cost_price, " +
                     "stock_quantity, min_stock_level, category_id, image_url, images, weight, dimensions, " +
                     "is_active, is_featured, created_at, updated_at FROM products " +
                     "WHERE is_active = true AND category_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        return DatabaseUtil.queryList(sql, this::mapResultSetToProduct, categoryId, limit, offset);
    }
    
    /**
     * 获取推荐商品
     * @param limit 限制数量
     * @return 推荐商品列表
     */
    public List<Product> findFeatured(int limit) {
        String sql = "SELECT id, name, description, short_description, sku, price, cost_price, " +
                     "stock_quantity, min_stock_level, category_id, image_url, images, weight, dimensions, " +
                     "is_active, is_featured, created_at, updated_at FROM products " +
                     "WHERE is_active = true AND is_featured = true ORDER BY created_at DESC LIMIT ?";
        
        return DatabaseUtil.queryList(sql, this::mapResultSetToProduct, limit);
    }
    
    /**
     * 搜索商品
     * @param keyword 搜索关键词
     * @param categoryId 分类ID（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @param inStock 是否只显示有库存商品
     * @param sortBy 排序字段（name, price, created_at）
     * @param sortOrder 排序方向（ASC, DESC）
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 商品列表
     */
    public List<Product> searchProducts(String keyword, Long categoryId, Double minPrice, Double maxPrice,
                                    Boolean inStock, String sortBy, String sortOrder, int limit, int offset) {
        
        StringBuilder sql = new StringBuilder(
            "SELECT id, name, description, short_description, sku, price, cost_price, " +
            "stock_quantity, min_stock_level, category_id, image_url, images, weight, dimensions, " +
            "is_active, is_featured, created_at, updated_at FROM products WHERE is_active = true");
        
        java.util.List<Object> params = new java.util.ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ? OR short_description LIKE ?)");
            String searchPattern = "%" + keyword + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        
        if (inStock != null && inStock) {
            sql.append(" AND stock_quantity > 0");
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            String validSortColumn;
            switch (sortBy) {
                case "price":
                case "created_at":
                case "name":
                    validSortColumn = sortBy;
                    break;
                default:
                    validSortColumn = "created_at";
            }
            
            sql.append(" ORDER BY ").append(validSortColumn);
            
            if (sortOrder != null && sortOrder.equalsIgnoreCase("ASC")) {
                sql.append(" ASC");
            } else {
                sql.append(" DESC");
            }
        } else {
            sql.append(" ORDER BY created_at DESC");
        }
        
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        return DatabaseUtil.queryList(sql.toString(), this::mapResultSetToProduct, params.toArray());
    }
    
    /**
     * 获取活跃商品总数
     * @return 总数
     */
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = true";
        return DatabaseUtil.count(sql);
    }
    
    /**
     * 根据分类获取商品总数
     * @param categoryId 分类ID
     * @return 总数
     */
    public long countByCategory(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = true AND category_id = ?";
        return DatabaseUtil.count(sql, categoryId);
    }
    
    /**
     * 搜索商品总数
     * @param keyword 搜索关键词
     * @param categoryId 分类ID（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @param inStock 是否只显示有库存商品
     * @return 总数
     */
    public long countSearchProducts(String keyword, Long categoryId, Double minPrice, Double maxPrice, Boolean inStock) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE is_active = true");
        java.util.List<Object> params = new java.util.ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ? OR short_description LIKE ?)");
            String searchPattern = "%" + keyword + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        
        if (inStock != null && inStock) {
            sql.append(" AND stock_quantity > 0");
        }
        
        return DatabaseUtil.count(sql.toString(), params.toArray());
    }
    
    /**
     * 更新商品库存
     * @param productId 商品ID
     * @param quantity 新库存数量
     * @return 是否成功
     */
    public boolean updateStock(Long productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, quantity, productId);
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功更新商品库存，ID: {}, 新库存: {}", productId, quantity);
            } else {
                logger.warn("更新商品库存失败，未找到记录，ID: {}", productId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("更新商品库存失败，ID: {}", productId, e);
            throw new RuntimeException("更新商品库存失败", e);
        }
    }
    
    /**
     * 将ResultSet映射为Product对象
     * @param rs ResultSet
     * @return Product对象
     * @throws SQLException SQL异常
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setShortDescription(rs.getString("short_description"));
        product.setSku(rs.getString("sku"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCostPrice(rs.getBigDecimal("cost_price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setMinStockLevel(rs.getInt("min_stock_level"));
        product.setCategoryId(rs.getLong("category_id"));
        product.setImageUrl(rs.getString("image_url"));
        
        // 处理images字段（JSON格式）
        String imagesJson = rs.getString("images");
        if (imagesJson != null && !imagesJson.isEmpty()) {
            try {
                // 简单处理：如果是以[开头和]结尾的数组格式，进行简单解析
                if (imagesJson.startsWith("[") && imagesJson.endsWith("]")) {
                    String cleanJson = imagesJson.substring(1, imagesJson.length() - 1);
                    if (!cleanJson.trim().isEmpty()) {
                        String[] imageArray = cleanJson.split(",");
                        java.util.List<String> imageList = new java.util.ArrayList<>();
                        for (String image : imageArray) {
                            image = image.trim();
                            if (image.startsWith("\"") && image.endsWith("\"")) {
                                image = image.substring(1, image.length() - 1);
                            }
                            if (!image.isEmpty()) {
                                imageList.add(image);
                            }
                        }
                        product.setImages(imageList);
                    }
                }
            } catch (Exception e) {
                logger.warn("解析商品图片失败: {}", imagesJson, e);
            }
        }
        
        product.setWeight(rs.getBigDecimal("weight"));
        product.setDimensions(rs.getString("dimensions"));
        product.setActive(rs.getBoolean("is_active"));
        product.setFeatured(rs.getBoolean("is_featured"));
        product.setCreatedAt(rs.getTimestamp("created_at") != null ? 
                rs.getTimestamp("created_at").toLocalDateTime() : null);
        product.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
        
        return product;
    }
}