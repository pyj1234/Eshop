package com.cat.dao;

import com.cat.model.Category;
import com.cat.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CategoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);
    
    /**
     * 创建分类
     * @param category 分类对象
     * @return 创建的分类ID
     */
    public Long create(Category category) {
        String sql = "INSERT INTO categories (name, description, parent_id, image_url, sort_order, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            long id = DatabaseUtil.executeInsert(sql, 
                    category.getName(),
                    category.getDescription(),
                    category.getParentId(),
                    category.getImageUrl(),
                    category.getSortOrder(),
                    category.isActive());
            
            logger.info("成功创建分类，ID: {}, 名称: {}", id, category.getName());
            return id;
            
        } catch (Exception e) {
            logger.error("创建分类失败: {}", category.getName(), e);
            throw new RuntimeException("创建分类失败", e);
        }
    }
    
    /**
     * 根据ID查找分类
     * @param id 分类ID
     * @return 分类对象，如果不存在返回null
     */
    public Category findById(Long id) {
        String sql = "SELECT id, name, description, parent_id, image_url, sort_order, is_active, " +
                     "created_at FROM categories WHERE id = ?";
        
        return DatabaseUtil.querySingle(sql, this::mapResultSetToCategory, id);
    }
    
    /**
     * 获取所有顶级分类
     * @return 顶级分类列表
     */
    public List<Category> findRootCategories() {
        String sql = "SELECT id, name, description, parent_id, image_url, sort_order, is_active, " +
                     "created_at FROM categories WHERE parent_id IS NULL AND is_active = true " +
                     "ORDER BY sort_order ASC, name ASC";
        
        return DatabaseUtil.queryList(sql, this::mapResultSetToCategory);
    }
    
    /**
     * 根据父分类ID获取子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    public List<Category> findByParentId(Long parentId) {
        String sql = "SELECT id, name, description, parent_id, image_url, sort_order, is_active, " +
                     "created_at FROM categories WHERE parent_id = ? AND is_active = true " +
                     "ORDER BY sort_order ASC, name ASC";
        
        return DatabaseUtil.queryList(sql, this::mapResultSetToCategory, parentId);
    }
    
    /**
     * 获取所有活跃分类
     * @return 活跃分类列表
     */
    public List<Category> findAllActive() {
        String sql = "SELECT id, name, description, parent_id, image_url, sort_order, is_active, " +
                     "created_at FROM categories WHERE is_active = true " +
                     "ORDER BY sort_order ASC, name ASC";
        
        return DatabaseUtil.queryList(sql, this::mapResultSetToCategory);
    }
    
    /**
     * 获取分类树结构
     * @return 分类树列表
     */
    public List<Category> findCategoryTree() {
        List<Category> allCategories = findAllActive();
        
        // 构建分类树
        java.util.Map<Long, Category> categoryMap = new java.util.HashMap<>();
        List<Category> rootCategories = new java.util.ArrayList<>();
        
        // 首先将所有分类放入map中
        for (Category category : allCategories) {
            categoryMap.put(category.getId(), category);
        }
        
        // 然后建立父子关系
        for (Category category : allCategories) {
            if (category.isRoot()) {
                rootCategories.add(category);
            } else {
                Category parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new java.util.ArrayList<>());
                    }
                    parent.getChildren().add(category);
                    category.setParent(parent);
                }
            }
        }
        
        return rootCategories;
    }
    
    /**
     * 更新分类信息
     * @param category 分类对象
     * @return 是否成功
     */
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, parent_id = ?, image_url = ?, " +
                     "sort_order = ?, is_active = ? WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql,
                    category.getName(),
                    category.getDescription(),
                    category.getParentId(),
                    category.getImageUrl(),
                    category.getSortOrder(),
                    category.isActive(),
                    category.getId());
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功更新分类信息，ID: {}", category.getId());
            } else {
                logger.warn("更新分类信息失败，未找到记录，ID: {}", category.getId());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("更新分类信息失败，ID: {}", category.getId(), e);
            throw new RuntimeException("更新分类信息失败", e);
        }
    }
    
    /**
     * 删除分类（逻辑删除）
     * @param id 分类ID
     * @return 是否成功
     */
    public boolean delete(Long id) {
        // 首先检查是否有子分类
        List<Category> children = findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("该分类下还有子分类，无法删除");
        }
        
        // 检查是否有商品使用该分类
        long productCount = DatabaseUtil.count(
            "SELECT COUNT(*) FROM products WHERE category_id = ? AND is_active = true", id);
        if (productCount > 0) {
            throw new RuntimeException("该分类下还有商品，无法删除");
        }
        
        String sql = "UPDATE categories SET is_active = false WHERE id = ?";
        
        try {
            int rows = DatabaseUtil.executeUpdate(sql, id);
            
            boolean success = rows > 0;
            if (success) {
                logger.info("成功删除分类，ID: {}", id);
            } else {
                logger.warn("删除分类失败，未找到记录，ID: {}", id);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("删除分类失败，ID: {}", id, e);
            throw new RuntimeException("删除分类失败", e);
        }
    }
    
    /**
     * 检查分类名称是否存在
     * @param name 分类名称
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 是否存在
     */
    public boolean existsByName(String name, Long excludeId) {
        String sql;
        if (excludeId != null) {
            sql = "SELECT 1 FROM categories WHERE name = ? AND id != ?";
            return DatabaseUtil.exists(sql, name, excludeId);
        } else {
            sql = "SELECT 1 FROM categories WHERE name = ?";
            return DatabaseUtil.exists(sql, name);
        }
    }
    
    /**
     * 搜索分类
     * @param keyword 搜索关键词
     * @return 分类列表
     */
    public List<Category> searchCategories(String keyword) {
        String sql = "SELECT id, name, description, parent_id, image_url, sort_order, is_active, " +
                     "created_at FROM categories WHERE is_active = true AND " +
                     "(name LIKE ? OR description LIKE ?) ORDER BY sort_order ASC, name ASC";
        
        String searchPattern = "%" + keyword + "%";
        return DatabaseUtil.queryList(sql, this::mapResultSetToCategory, searchPattern, searchPattern);
    }
    
    /**
     * 获取分类路径（从根分类到当前分类）
     * @param categoryId 分类ID
     * @return 分类路径列表
     */
    public List<Category> getCategoryPath(Long categoryId) {
        List<Category> path = new java.util.ArrayList<>();
        Category current = findById(categoryId);
        
        while (current != null) {
            path.add(0, current); // 添加到列表开头
            if (!current.isRoot()) {
                current = findById(current.getParentId());
            } else {
                break;
            }
        }
        
        return path;
    }
    
    /**
     * 获取活跃分类总数
     * @return 总数
     */
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM categories WHERE is_active = true";
        return DatabaseUtil.count(sql);
    }
    
    /**
     * 将ResultSet映射为Category对象
     * @param rs ResultSet
     * @return Category对象
     * @throws SQLException SQL异常
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setParentId(rs.getLong("parent_id"));
        if (rs.wasNull()) {
            category.setParentId(null);
        }
        category.setImageUrl(rs.getString("image_url"));
        category.setSortOrder(rs.getInt("sort_order"));
        category.setActive(rs.getBoolean("is_active"));
        category.setCreatedAt(rs.getTimestamp("created_at") != null ? 
                rs.getTimestamp("created_at").toLocalDateTime() : null);
        
        return category;
    }
}