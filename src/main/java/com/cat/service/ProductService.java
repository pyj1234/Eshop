package com.cat.service;

import com.cat.dao.CategoryDAO;
import com.cat.dao.ProductDAO;
import com.cat.model.Category;
import com.cat.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    
    public ProductService() {
        this.productDAO = new ProductDAO();
        this.categoryDAO = new CategoryDAO();
    }
    
    /**
     * 创建商品
     * @param product 商品对象
     * @return 创建结果
     */
    public CreationResult createProduct(Product product) {
        // 验证输入参数
        String validationError = validateProductInput(product);
        if (validationError != null) {
            return new CreationResult(false, validationError, null);
        }
        
        // 检查SKU是否已存在
        if (productDAO.existsBySku(product.getSku())) {
            return new CreationResult(false, "SKU已存在", null);
        }
        
        // 检查分类是否存在
        if (product.getCategoryId() != null) {
            Category category = categoryDAO.findById(product.getCategoryId());
            if (category == null || !category.isActive()) {
                return new CreationResult(false, "指定的分类不存在或已禁用", null);
            }
        }
        
        try {
            Long productId = productDAO.create(product);
            product.setId(productId);
            
            logger.info("商品创建成功，ID: {}, 名称: {}", productId, product.getName());
            return new CreationResult(true, "商品创建成功", product);
            
        } catch (Exception e) {
            logger.error("商品创建失败，名称: {}", product.getName(), e);
            return new CreationResult(false, "商品创建失败，请稍后重试", null);
        }
    }
    
    /**
     * 更新商品信息
     * @param product 商品对象
     * @return 更新结果
     */
    public UpdateResult updateProduct(Product product) {
        // 验证输入参数
        String validationError = validateProductInput(product);
        if (validationError != null) {
            return new UpdateResult(false, validationError);
        }
        
        // 检查商品是否存在
        Product existingProduct = productDAO.findById(product.getId());
        if (existingProduct == null) {
            return new UpdateResult(false, "商品不存在");
        }
        
        // 检查SKU是否被其他商品占用
        Product productWithSameSku = productDAO.findBySku(product.getSku());
        if (productWithSameSku != null && !productWithSameSku.getId().equals(product.getId())) {
            return new UpdateResult(false, "SKU已被其他商品使用");
        }
        
        // 检查分类是否存在
        if (product.getCategoryId() != null) {
            Category category = categoryDAO.findById(product.getCategoryId());
            if (category == null || !category.isActive()) {
                return new UpdateResult(false, "指定的分类不存在或已禁用");
            }
        }
        
        try {
            boolean success = productDAO.update(product);
            
            if (success) {
                logger.info("商品信息更新成功，ID: {}", product.getId());
                return new UpdateResult(true, "商品信息更新成功");
            } else {
                return new UpdateResult(false, "商品信息更新失败");
            }
            
        } catch (Exception e) {
            logger.error("商品信息更新失败，ID: {}", product.getId(), e);
            return new UpdateResult(false, "商品信息更新失败，请稍后重试");
        }
    }
    
    /**
     * 删除商品（逻辑删除）
     * @param productId 商品ID
     * @return 删除结果
     */
    public UpdateResult deleteProduct(Long productId) {
        try {
            // 检查商品是否存在
            Product product = productDAO.findById(productId);
            if (product == null) {
                return new UpdateResult(false, "商品不存在");
            }
            
            boolean success = productDAO.delete(productId);
            
            if (success) {
                logger.info("商品删除成功，ID: {}", productId);
                return new UpdateResult(true, "商品删除成功");
            } else {
                return new UpdateResult(false, "商品删除失败");
            }
            
        } catch (Exception e) {
            logger.error("商品删除失败，ID: {}", productId, e);
            return new UpdateResult(false, "商品删除失败，请稍后重试");
        }
    }
    
    /**
     * 获取商品详情
     * @param productId 商品ID
     * @return 商品对象
     */
    public Product getProductById(Long productId) {
        try {
            Product product = productDAO.findById(productId);
            if (product != null && product.getCategoryId() != null) {
                // 加载分类信息
                Category category = categoryDAO.findById(product.getCategoryId());
                product.setCategory(category);
            }
            return product;
        } catch (Exception e) {
            logger.error("获取商品详情失败，ID: {}", productId, e);
            return null;
        }
    }
    
    /**
     * 根据SKU获取商品
     * @param sku SKU
     * @return 商品对象
     */
    public Product getProductBySku(String sku) {
        try {
            return productDAO.findBySku(sku);
        } catch (Exception e) {
            logger.error("根据SKU获取商品失败，SKU: {}", sku, e);
            return null;
        }
    }
    
    /**
     * 获取商品列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 商品列表
     */
    public List<Product> getProductList(int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            List<Product> products = productDAO.findAllActive(pageSize, offset);
            
            // 为每个商品加载分类信息
            for (Product product : products) {
                if (product.getCategoryId() != null) {
                    Category category = categoryDAO.findById(product.getCategoryId());
                    product.setCategory(category);
                }
            }
            
            return products;
        } catch (Exception e) {
            logger.error("获取商品列表失败，页码: {}, 每页大小: {}", page, pageSize, e);
            throw new RuntimeException("获取商品列表失败", e);
        }
    }
    
    /**
     * 根据分类获取商品列表
     * @param categoryId 分类ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 商品列表
     */
    public List<Product> getProductsByCategory(Long categoryId, int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            List<Product> products = productDAO.findByCategory(categoryId, pageSize, offset);
            
            // 为每个商品加载分类信息
            Category category = categoryDAO.findById(categoryId);
            for (Product product : products) {
                product.setCategory(category);
            }
            
            return products;
        } catch (Exception e) {
            logger.error("根据分类获取商品列表失败，分类ID: {}, 页码: {}", categoryId, page, e);
            throw new RuntimeException("获取商品列表失败", e);
        }
    }
    
    /**
     * 获取推荐商品
     * @param limit 数量限制
     * @return 推荐商品列表
     */
    public List<Product> getFeaturedProducts(int limit) {
        try {
            List<Product> products = productDAO.findFeatured(limit);
            
            // 为每个商品加载分类信息
            for (Product product : products) {
                if (product.getCategoryId() != null) {
                    Category category = categoryDAO.findById(product.getCategoryId());
                    product.setCategory(category);
                }
            }
            
            return products;
        } catch (Exception e) {
            logger.error("获取推荐商品失败，限制: {}", limit, e);
            throw new RuntimeException("获取推荐商品失败", e);
        }
    }
    
    /**
     * 搜索商品
     * @param searchParams 搜索参数
     * @return 搜索结果
     */
    public SearchResult searchProducts(SearchParams searchParams) {
        try {
            List<Product> products = productDAO.searchProducts(
                searchParams.getKeyword(),
                searchParams.getCategoryId(),
                searchParams.getMinPrice(),
                searchParams.getMaxPrice(),
                searchParams.getInStock(),
                searchParams.getSortBy(),
                searchParams.getSortOrder(),
                searchParams.getPageSize(),
                (searchParams.getPage() - 1) * searchParams.getPageSize()
            );
            
            // 为每个商品加载分类信息
            for (Product product : products) {
                if (product.getCategoryId() != null) {
                    Category category = categoryDAO.findById(product.getCategoryId());
                    product.setCategory(category);
                }
            }
            
            // 获取总数
            long totalCount = productDAO.countSearchProducts(
                searchParams.getKeyword(),
                searchParams.getCategoryId(),
                searchParams.getMinPrice(),
                searchParams.getMaxPrice(),
                searchParams.getInStock()
            );
            
            return new SearchResult(products, totalCount, searchParams.getPage(), searchParams.getPageSize());
            
        } catch (Exception e) {
            logger.error("搜索商品失败", e);
            throw new RuntimeException("搜索商品失败", e);
        }
    }
    
    /**
     * 获取商品总数
     * @return 总数
     */
    public long getProductCount() {
        try {
            return productDAO.countActive();
        } catch (Exception e) {
            logger.error("获取商品总数失败", e);
            throw new RuntimeException("获取商品总数失败", e);
        }
    }
    
    /**
     * 根据分类获取商品总数
     * @param categoryId 分类ID
     * @return 总数
     */
    public long getProductCountByCategory(Long categoryId) {
        try {
            return productDAO.countByCategory(categoryId);
        } catch (Exception e) {
            logger.error("根据分类获取商品总数失败，分类ID: {}", categoryId, e);
            throw new RuntimeException("获取商品总数失败", e);
        }
    }
    
    /**
     * 更新商品库存
     * @param productId 商品ID
     * @param quantity 新库存数量
     * @return 更新结果
     */
    public UpdateResult updateProductStock(Long productId, int quantity) {
        try {
            // 检查商品是否存在
            Product product = productDAO.findById(productId);
            if (product == null) {
                return new UpdateResult(false, "商品不存在");
            }
            
            if (quantity < 0) {
                return new UpdateResult(false, "库存数量不能为负数");
            }
            
            boolean success = productDAO.updateStock(productId, quantity);
            
            if (success) {
                logger.info("商品库存更新成功，ID: {}, 新库存: {}", productId, quantity);
                return new UpdateResult(true, "商品库存更新成功");
            } else {
                return new UpdateResult(false, "商品库存更新失败");
            }
            
        } catch (Exception e) {
            logger.error("商品库存更新失败，ID: {}", productId, e);
            return new UpdateResult(false, "商品库存更新失败，请稍后重试");
        }
    }
    
    /**
     * 验证商品输入参数
     */
    private String validateProductInput(Product product) {
        if (product == null) {
            return "商品信息不能为空";
        }
        
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return "商品名称不能为空";
        }
        
        if (product.getName().length() > 200) {
            return "商品名称长度不能超过200个字符";
        }
        
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            return "SKU不能为空";
        }
        
        if (product.getSku().length() > 100) {
            return "SKU长度不能超过100个字符";
        }
        
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return "商品价格必须大于等于0";
        }
        
        if (product.getStockQuantity() < 0) {
            return "商品库存不能为负数";
        }
        
        if (product.getMinStockLevel() < 0) {
            return "最小库存水平不能为负数";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 创建结果类
     */
    public static class CreationResult {
        private final boolean success;
        private final String message;
        private final Product product;
        
        public CreationResult(boolean success, String message, Product product) {
            this.success = success;
            this.message = message;
            this.product = product;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Product getProduct() { return product; }
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
    
    /**
     * 搜索参数类
     */
    public static class SearchParams {
        private String keyword;
        private Long categoryId;
        private Double minPrice;
        private Double maxPrice;
        private Boolean inStock;
        private String sortBy = "created_at";
        private String sortOrder = "DESC";
        private int page = 1;
        private int pageSize = 10;
        
        // Getters and Setters
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        
        public Double getMinPrice() { return minPrice; }
        public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
        
        public Double getMaxPrice() { return maxPrice; }
        public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
        
        public Boolean getInStock() { return inStock; }
        public void setInStock(Boolean inStock) { this.inStock = inStock; }
        
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
        
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    }
    
    /**
     * 搜索结果类
     */
    public static class SearchResult {
        private final List<Product> products;
        private final long totalCount;
        private final int currentPage;
        private final int pageSize;
        private final long totalPages;
        
        public SearchResult(List<Product> products, long totalCount, int currentPage, int pageSize) {
            this.products = products;
            this.totalCount = totalCount;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalPages = (totalCount + pageSize - 1) / pageSize;
        }
        
        public List<Product> getProducts() { return products; }
        public long getTotalCount() { return totalCount; }
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
        public long getTotalPages() { return totalPages; }
    }
}