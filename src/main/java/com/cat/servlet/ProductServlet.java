package com.cat.servlet;

import com.cat.dao.CategoryDAO;
import com.cat.dto.ApiResponse;
import com.cat.model.Category;
import com.cat.model.Product;
import com.cat.service.ProductService;
import com.cat.util.JsonUtil;
import com.cat.util.RequestUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/products/*")
public class ProductServlet extends HttpServlet {
    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    
    private void writeJsonResponse(HttpServletResponse response, Object obj) throws IOException {
        response.getWriter().write(JsonUtil.toJson(obj));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetProducts(request, response);
            } else if (pathInfo.equals("/featured")) {
                handleGetFeaturedProducts(request, response);
            } else if (pathInfo.equals("/search")) {
                handleSearchProducts(request, response);
            } else if (pathInfo.equals("/categories")) {
                handleGetCategories(request, response);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetProductById(request, response, pathInfo);
            } else if (pathInfo.equals("/category/\\d+")) {
                handleGetProductsByCategory(request, response, pathInfo);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                handleCreateProduct(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                handleUpdateProduct(request, response, pathInfo);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/stock")) {
                handleUpdateProductStock(request, response, pathInfo);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                handleDeleteProduct(request, response, pathInfo);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("未找到对应的API端点"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("服务器内部错误"));
        }
    }
    
    /**
     * 处理获取商品列表
     */
    private void handleGetProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int page = RequestUtil.getIntParameter(request, "page", 1);
            int pageSize = RequestUtil.getIntParameter(request, "pageSize", 10);
            
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            List<Product> products = productService.getProductList(page, pageSize);
            long totalCount = productService.getProductCount();
            
            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("page", page);
            pageInfo.put("pageSize", pageSize);
            pageInfo.put("totalCount", totalCount);
            pageInfo.put("totalPages", (totalCount + pageSize - 1) / pageSize);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("products", products);
            responseData.put("pageInfo", pageInfo);
            
            ApiResponse<Map<String, Object>> responseObj = ApiResponse.success("获取成功", responseData);
            writeJsonResponse(response, responseObj);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数错误"));
        }
    }
    
    /**
     * 处理获取推荐商品
     */
    private void handleGetFeaturedProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int limit = RequestUtil.getIntParameter(request, "limit", 10);
            if (limit < 1 || limit > 50) limit = 10;
            
            List<Product> products = productService.getFeaturedProducts(limit);
            ApiResponse<List<Product>> responseObj = ApiResponse.success("获取成功", products);
            writeJsonResponse(response, responseObj);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数错误"));
        }
    }
    
    /**
     * 处理搜索商品
     */
    private void handleSearchProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ProductService.SearchParams searchParams = new ProductService.SearchParams();
            
            searchParams.setKeyword(RequestUtil.getParameter(request, "keyword", ""));
            String categoryIdStr = RequestUtil.getParameter(request, "categoryId", "");
            if (!categoryIdStr.isEmpty()) {
                searchParams.setCategoryId(Long.parseLong(categoryIdStr));
            }
            
            String minPriceStr = RequestUtil.getParameter(request, "minPrice", "");
            if (!minPriceStr.isEmpty()) {
                searchParams.setMinPrice(Double.parseDouble(minPriceStr));
            }
            
            String maxPriceStr = RequestUtil.getParameter(request, "maxPrice", "");
            if (!maxPriceStr.isEmpty()) {
                searchParams.setMaxPrice(Double.parseDouble(maxPriceStr));
            }
            
            searchParams.setInStock(RequestUtil.getBooleanParameter(request, "inStock", false));
            searchParams.setSortBy(RequestUtil.getParameter(request, "sortBy", "created_at"));
            searchParams.setSortOrder(RequestUtil.getParameter(request, "sortOrder", "DESC"));
            searchParams.setPage(RequestUtil.getIntParameter(request, "page", 1));
            searchParams.setPageSize(RequestUtil.getIntParameter(request, "pageSize", 10));
            
            if (searchParams.getPage() < 1) searchParams.setPage(1);
            if (searchParams.getPageSize() < 1 || searchParams.getPageSize() > 100) {
                searchParams.setPageSize(10);
            }
            
            ProductService.SearchResult searchResult = productService.searchProducts(searchParams);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("products", searchResult.getProducts());
            responseData.put("totalCount", searchResult.getTotalCount());
            responseData.put("currentPage", searchResult.getCurrentPage());
            responseData.put("pageSize", searchResult.getPageSize());
            responseData.put("totalPages", searchResult.getTotalPages());
            
            ApiResponse<Map<String, Object>> responseObj = ApiResponse.success("搜索成功", responseData);
            writeJsonResponse(response, responseObj);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数错误"));
        }
    }
    
    /**
     * 处理获取分类列表
     */
    private void handleGetCategories(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            boolean includeTree = RequestUtil.getBooleanParameter(request, "tree", false);
            
            List<Category> categories;
            if (includeTree) {
                categories = categoryDAO.findCategoryTree();
            } else {
                categories = categoryDAO.findAllActive();
            }
            
            ApiResponse<List<Category>> responseObj = ApiResponse.success("获取成功", categories);
            writeJsonResponse(response, responseObj);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, ApiResponse.error("获取分类失败"));
        }
    }
    
    /**
     * 处理根据分类获取商品
     */
    private void handleGetProductsByCategory(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            // 从路径中提取分类ID
            Long categoryId = Long.parseLong(pathInfo.substring("/category/".length()));
            
            int page = RequestUtil.getIntParameter(request, "page", 1);
            int pageSize = RequestUtil.getIntParameter(request, "pageSize", 10);
            
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            
            List<Product> products = productService.getProductsByCategory(categoryId, page, pageSize);
            long totalCount = productService.getProductCountByCategory(categoryId);
            
            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("page", page);
            pageInfo.put("pageSize", pageSize);
            pageInfo.put("totalCount", totalCount);
            pageInfo.put("totalPages", (totalCount + pageSize - 1) / pageSize);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("products", products);
            responseData.put("categoryId", categoryId);
            responseData.put("pageInfo", pageInfo);
            
            ApiResponse<Map<String, Object>> responseObj = ApiResponse.success("获取成功", responseData);
            writeJsonResponse(response, responseObj);
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("分类ID格式错误"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数错误"));
        }
    }
    
    /**
     * 处理获取指定商品详情
     */
    private void handleGetProductById(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            Product product = productService.getProductById(productId);
            
            if (product != null) {
                ApiResponse<Product> responseObj = ApiResponse.success("获取成功", product);
                writeJsonResponse(response, responseObj);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJsonResponse(response, ApiResponse.error("商品不存在"));
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("商品ID格式错误"));
        }
    }
    
    /**
     * 处理创建商品（管理员功能）
     */
    private void handleCreateProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            Product product = new Product();
            product.setName((String) requestData.get("name"));
            product.setDescription((String) requestData.get("description"));
            product.setShortDescription((String) requestData.get("shortDescription"));
            product.setSku((String) requestData.get("sku"));
            
            String priceStr = (String) requestData.get("price");
            if (priceStr != null) {
                product.setPrice(new java.math.BigDecimal(priceStr));
            }
            
            String costPriceStr = (String) requestData.get("costPrice");
            if (costPriceStr != null) {
                product.setCostPrice(new java.math.BigDecimal(costPriceStr));
            }
            
            Object stockQuantityObj = requestData.get("stockQuantity");
            if (stockQuantityObj != null) {
                product.setStockQuantity(((Number) stockQuantityObj).intValue());
            }
            
            Object minStockLevelObj = requestData.get("minStockLevel");
            if (minStockLevelObj != null) {
                product.setMinStockLevel(((Number) minStockLevelObj).intValue());
            }
            
            Object categoryIdObj = requestData.get("categoryId");
            if (categoryIdObj != null) {
                product.setCategoryId(((Number) categoryIdObj).longValue());
            }
            
            product.setImageUrl((String) requestData.get("imageUrl"));
            
            String weightStr = (String) requestData.get("weight");
            if (weightStr != null) {
                product.setWeight(new java.math.BigDecimal(weightStr));
            }
            
            product.setDimensions((String) requestData.get("dimensions"));
            product.setActive(RequestUtil.getBooleanParameter(request, "isActive", true));
            product.setFeatured(RequestUtil.getBooleanParameter(request, "isFeatured", false));
            
            ProductService.CreationResult result = productService.createProduct(product);
            
            if (result.isSuccess()) {
                ApiResponse<Product> responseObj = ApiResponse.success("商品创建成功", result.getProduct());
                writeJsonResponse(response, responseObj);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数格式错误"));
        }
    }
    
    /**
     * 处理更新商品信息
     */
    private void handleUpdateProduct(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            Product product = new Product();
            product.setId(productId);
            product.setName((String) requestData.get("name"));
            product.setDescription((String) requestData.get("description"));
            product.setShortDescription((String) requestData.get("shortDescription"));
            product.setSku((String) requestData.get("sku"));
            
            String priceStr = (String) requestData.get("price");
            if (priceStr != null) {
                product.setPrice(new java.math.BigDecimal(priceStr));
            }
            
            String costPriceStr = (String) requestData.get("costPrice");
            if (costPriceStr != null) {
                product.setCostPrice(new java.math.BigDecimal(costPriceStr));
            }
            
            Object stockQuantityObj = requestData.get("stockQuantity");
            if (stockQuantityObj != null) {
                product.setStockQuantity(((Number) stockQuantityObj).intValue());
            }
            
            Object minStockLevelObj = requestData.get("minStockLevel");
            if (minStockLevelObj != null) {
                product.setMinStockLevel(((Number) minStockLevelObj).intValue());
            }
            
            Object categoryIdObj = requestData.get("categoryId");
            if (categoryIdObj != null) {
                product.setCategoryId(((Number) categoryIdObj).longValue());
            }
            
            product.setImageUrl((String) requestData.get("imageUrl"));
            
            String weightStr = (String) requestData.get("weight");
            if (weightStr != null) {
                product.setWeight(new java.math.BigDecimal(weightStr));
            }
            
            product.setDimensions((String) requestData.get("dimensions"));
            product.setActive(RequestUtil.getBooleanParameter(request, "isActive", true));
            product.setFeatured(RequestUtil.getBooleanParameter(request, "isFeatured", false));
            
            ProductService.UpdateResult result = productService.updateProduct(product);
            
            if (result.isSuccess()) {
                writeJsonResponse(response, ApiResponse.success(result.getMessage()));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("商品ID格式错误"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数格式错误"));
        }
    }
    
    /**
     * 处理更新商品库存
     */
    private void handleUpdateProductStock(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            Long productId = Long.parseLong(pathInfo.substring(1, pathInfo.indexOf("/stock")));
            
            String requestBody = RequestUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = JsonUtil.fromJsonToMap(requestBody);
            
            Object quantityObj = requestData.get("quantity");
            if (quantityObj == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error("库存数量不能为空"));
                return;
            }
            
            int quantity = ((Number) quantityObj).intValue();
            
            ProductService.UpdateResult result = productService.updateProductStock(productId, quantity);
            
            if (result.isSuccess()) {
                writeJsonResponse(response, ApiResponse.success(result.getMessage()));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("商品ID格式错误"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("请求参数格式错误"));
        }
    }
    
    /**
     * 处理删除商品
     */
    private void handleDeleteProduct(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            Long productId = Long.parseLong(pathInfo.substring(1));
            
            ProductService.UpdateResult result = productService.deleteProduct(productId);
            
            if (result.isSuccess()) {
                writeJsonResponse(response, ApiResponse.success(result.getMessage()));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, ApiResponse.error(result.getMessage()));
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJsonResponse(response, ApiResponse.error("商品ID格式错误"));
        }
    }
}