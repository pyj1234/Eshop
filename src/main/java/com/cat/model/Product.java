package com.cat.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Product {
    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private String sku;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int stockQuantity;
    private int minStockLevel;
    private Long categoryId;
    private String imageUrl;
    private List<String> images; // 多张图片URL列表
    private BigDecimal weight;
    private String dimensions; // 尺寸信息，可以是JSON字符串
    private boolean isActive;
    private boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联的分类信息
    private Category category;
    
    public Product() {}
    
    public Product(String name, String sku, BigDecimal price, int stockQuantity) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.isActive = true;
        this.isFeatured = false;
        this.minStockLevel = 10;
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
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getCostPrice() {
        return costPrice;
    }
    
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }
    
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public int getMinStockLevel() {
        return minStockLevel;
    }
    
    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public String getDimensions() {
        return dimensions;
    }
    
    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isFeatured() {
        return isFeatured;
    }
    
    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    /**
     * 检查是否有库存
     * @return 是否有库存
     */
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    /**
     * 检查库存是否低于最小库存水平
     * @return 是否库存不足
     */
    public boolean isLowStock() {
        return stockQuantity <= minStockLevel;
    }
    
    /**
     * 计算利润
     * @return 利润
     */
    public BigDecimal getProfit() {
        if (costPrice != null && price != null) {
            return price.subtract(costPrice);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 计算利润率
     * @return 利润率（百分比）
     */
    public BigDecimal getProfitMargin() {
        if (costPrice != null && price != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
            return getProfit().divide(costPrice, 4, BigDecimal.ROUND_HALF_UP)
                              .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", categoryId=" + categoryId +
                ", isActive=" + isActive +
                ", isFeatured=" + isFeatured +
                ", createdAt=" + createdAt +
                '}';
    }
}