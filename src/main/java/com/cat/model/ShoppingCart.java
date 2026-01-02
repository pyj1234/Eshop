package com.cat.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShoppingCart {
    private Long id;
    private Long customerId;
    private Long productId;
    private int quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联信息
    private Customer customer;
    private Product product;
    
    public ShoppingCart() {}
    
    public ShoppingCart(Long customerId, Long productId, int quantity) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    /**
     * 计算小计金额
     * @return 小计金额
     */
    public BigDecimal getSubtotal() {
        if (product != null && product.getPrice() != null) {
            return product.getPrice().multiply(new BigDecimal(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 检查商品是否有足够库存
     * @return 是否有足够库存
     */
    public boolean hasEnoughStock() {
        return product != null && product.isInStock() && product.getStockQuantity() >= quantity;
    }
    
    /**
     * 增加数量
     * @param amount 增加的数量
     */
    public void addQuantity(int amount) {
        this.quantity += amount;
    }
    
    /**
     * 减少数量
     * @param amount 减少的数量
     */
    public void subtractQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }
    
    @Override
    public String toString() {
        return "ShoppingCart{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                '}';
    }
}