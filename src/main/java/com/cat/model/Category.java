package com.cat.model;

import java.time.LocalDateTime;
import java.util.List;

public class Category {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String imageUrl;
    private int sortOrder;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    // 关联信息
    private Category parent;
    private List<Category> children;
    
    public Category() {}
    
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.sortOrder = 0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Category getParent() {
        return parent;
    }
    
    public void setParent(Category parent) {
        this.parent = parent;
    }
    
    public List<Category> getChildren() {
        return children;
    }
    
    public void setChildren(List<Category> children) {
        this.children = children;
    }
    
    /**
     * 检查是否为顶级分类
     * @return 是否为顶级分类
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }
    
    /**
     * 检查是否有子分类
     * @return 是否有子分类
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", sortOrder=" + sortOrder +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}